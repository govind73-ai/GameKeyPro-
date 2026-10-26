package com.gamekeypro.service
import android.app.*
import android.content.Intent
import android.graphics.PixelFormat
import android.os.*
import android.view.*
import android.widget.FrameLayout
import androidx.core.app.NotificationCompat
import com.gamekeypro.R
import com.gamekeypro.engine.TouchInjector

class OverlayService : Service(), View.OnKeyListener, View.OnGenericMotionListener {
    private lateinit var windowManager: WindowManager
    private lateinit var overlayView: FrameLayout
    private var isAimingMode = false
    private var aimCenterX = 1500f
    private var aimCenterY = 500f
    private var currentAimX = 1500f
    private var currentAimY = 500f

    override fun onBind(intent: Intent?): IBinder? = null
    override fun onCreate() {
        super.onCreate()
        val channelId = "gamekeypro_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "GameKeyPro", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }
        val notification = NotificationCompat.Builder(this, channelId).setContentTitle("GameKeyPro Active").setContentText("Mapping engine running").setSmallIcon(R.drawable.ic_stat_gamepad).build()
        startForeground(1001, notification)

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        TouchInjector.initialize()

        val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY else WindowManager.LayoutParams.TYPE_PHONE
        val params = WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT, layoutParamsType, WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, PixelFormat.TRANSLUCENT).apply { gravity = Gravity.TOP or Gravity.START }
        overlayView = FrameLayout(this).apply {
            isFocusable = true
            isFocusableInTouchMode = true
            setOnKeyListener(this@OverlayService)
            setOnGenericMotionListener(this@OverlayService)
        }
        windowManager.addView(overlayView, params)
        overlayView.requestFocus()
    }

    override fun onKey(v: View?, keyCode: Int, event: KeyEvent?): Boolean {
        if (event == null) return false
        if (keyCode == KeyEvent.KEYCODE_GRAVE && event.action == KeyEvent.ACTION_DOWN) {
            isAimingMode = !isAimingMode
            if (isAimingMode) { overlayView.requestPointerCapture(); TouchInjector.injectTouch(MotionEvent.ACTION_DOWN, aimCenterX, aimCenterY, 2) }
            else { overlayView.releasePointerCapture(); TouchInjector.injectTouch(MotionEvent.ACTION_UP, currentAimX, currentAimY, 2) }
            return true
        }
        if (keyCode == KeyEvent.KEYCODE_W) {
            when (event.action) {
                KeyEvent.ACTION_DOWN -> TouchInjector.injectTouch(MotionEvent.ACTION_DOWN, 450f, 750f, 1)
                KeyEvent.ACTION_UP -> TouchInjector.injectTouch(MotionEvent.ACTION_UP, 450f, 750f, 1)
            }
            return true
        }
        return false
    }

    override fun onGenericMotion(v: View?, event: MotionEvent?): Boolean {
        if (event == null || !isAimingMode) return false
        if (event.source and InputDevice.SOURCE_MOUSE != 0) {
            currentAimX += event.getAxisValue(MotionEvent.AXIS_RELATIVE_X) * 1.5f
            currentAimY += event.getAxisValue(MotionEvent.AXIS_RELATIVE_Y) * 1.5f
            TouchInjector.injectTouch(MotionEvent.ACTION_MOVE, currentAimX, currentAimY, 2)
            if (Math.hypot((currentAimX - aimCenterX).toDouble(), (currentAimY - aimCenterY).toDouble()) > 400) {
                TouchInjector.injectTouch(MotionEvent.ACTION_UP, currentAimX, currentAimY, 2)
                currentAimX = aimCenterX; currentAimY = aimCenterY
                TouchInjector.injectTouch(MotionEvent.ACTION_DOWN, currentAimX, currentAimY, 2)
            }
            return true
        }
        return false
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::overlayView.isInitialized) windowManager.removeView(overlayView)
    }
}
