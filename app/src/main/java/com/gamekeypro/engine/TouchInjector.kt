package com.gamekeypro.engine
import android.os.IBinder
import android.os.SystemClock
import android.util.Log
import android.view.InputDevice
import android.view.InputEvent
import android.view.MotionEvent
import rikka.shizuku.Shizuku
import rikka.shizuku.ShizukuBinderWrapper
import rikka.shizuku.SystemServiceHelper
import java.lang.reflect.Method

object TouchInjector {
    private var injectInputEventMethod: Method? = null
    private var inputManagerInstance: Any? = null
    private var isInitialized = false

    fun initialize(): Boolean {
        if (isInitialized) return true
        return try {
            if (!Shizuku.pingBinder()) return false
            val rawBinder = SystemServiceHelper.getSystemService("input") ?: return false
            val binder: IBinder = ShizukuBinderWrapper(rawBinder)
            val serviceManagerClass = Class.forName("android.hardware.input.IInputManager\$Stub")
            val asInterfaceMethod = serviceManagerClass.getMethod("asInterface", IBinder::class.java)
            inputManagerInstance = asInterfaceMethod.invoke(null, binder)
            injectInputEventMethod = inputManagerInstance?.javaClass?.getMethod("injectInputEvent", InputEvent::class.java, Int::class.javaPrimitiveType)
            isInitialized = (injectInputEventMethod != null)
            isInitialized
        } catch (e: Exception) { false }
    }

    fun injectTouch(action: Int, x: Float, y: Float, pointerId: Int = 0): Boolean {
        if (!isInitialized && !initialize()) return false
        val now = SystemClock.uptimeMillis()
        val properties = arrayOf(MotionEvent.PointerProperties().apply { id = pointerId; toolType = MotionEvent.TOOL_TYPE_FINGER })
        val coords = arrayOf(MotionEvent.PointerCoords().apply { this.x = x; this.y = y; pressure = 1.0f; size = 1.0f })
        val event = MotionEvent.obtain(now, now, action, 1, properties, coords, 0, 0, 1.0f, 1.0f, 0, 0, InputDevice.SOURCE_TOUCHSCREEN, 0)
        return try {
            injectInputEventMethod?.invoke(inputManagerInstance, event, 0)
            true
        } catch (e: Exception) { false } finally { event.recycle() }
    }
}
