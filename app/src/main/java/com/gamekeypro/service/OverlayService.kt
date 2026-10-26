package com.gamekeypro.service

import android.app.*
import android.content.Intent
import android.graphics.Color
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.gamekeypro.R
import com.gamekeypro.engine.TouchInjector

class OverlayService : Service() {

    private lateinit var windowManager: WindowManager
    private var floatingView: View? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onCreate() {
        super.onCreate()
        startForegroundNotification()

        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        TouchInjector.initialize()

        val layoutParamsType = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        // FIX: WRAP_CONTENT taaki sirf chhota icon bane, aur FLAG_NOT_FOCUSABLE taaki phone ka touch band na ho
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            layoutParamsType,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP or Gravity.START
            x = 20
            y = 200
        }

        // Chhota sa floating pill icon (🎮 GKP)
        val bubble = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(30, 20, 30, 20)
            setBackgroundColor(Color.parseColor("#E6212121"))
        }

        val text = TextView(this).apply {
            text = "🎮 GKP"
            setTextColor(Color.WHITE)
            textSize = 15f
        }
        bubble.addView(text)

        // Floating icon ko ungli se screen par idhar-udhar khiskane (drag) ke liye
        bubble.setOnTouchListener(object : View.OnTouchListener {
            private var initialX = 0
            private var initialY = 0
            private var initialTouchX = 0f
            private var initialTouchY = 0f

            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event == null) return false
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        initialX = params.x
                        initialY = params.y
                        initialTouchX = event.rawX
                        initialTouchY = event.rawY
                        return true
                    }
                    MotionEvent.ACTION_MOVE -> {
                        params.x = initialX + (event.rawX - initialTouchX).toInt()
                        params.y = initialY + (event.rawY - initialTouchY).toInt()
                        windowManager.updateViewLayout(bubble, params)
                        return true
                    }
                    MotionEvent.ACTION_UP -> {
                        val diffX = Math.abs(event.rawX - initialTouchX)
                        val diffY = Math.abs(event.rawY - initialTouchY)
                        if (diffX < 15 && diffY < 15) {
                            Toast.makeText(this@OverlayService, "GameKeyPro Running! Touch working perfectly.", Toast.LENGTH_SHORT).show()
                        }
                        return true
                    }
                }
                return false
            }
        })

        floatingView = bubble
        windowManager.addView(floatingView, params)
    }

    private fun startForegroundNotification() {
        val channelId = "gamekeypro_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "GameKeyPro Engine",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)?.createNotificationChannel(channel)
        }

        val stopIntent = Intent(this, OverlayService::class.java).apply {
            action = "STOP_SERVICE"
        }
        val stopPendingIntent = PendingIntent.getService(
            this, 0, stopIntent,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("GameKeyPro Active")
            .setContentText("Tap 'STOP' to close overlay safely.")
            .setSmallIcon(R.drawable.ic_stat_gamepad)
            .addAction(android.R.drawable.ic_menu_close_clear_cancel, "STOP", stopPendingIntent)
            .setOngoing(true)
            .build()

        startForeground(1001, notification)
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "STOP_SERVICE") {
            stopSelf()
            return START_NOT_STICKY
        }
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (floatingView != null) {
            windowManager.removeView(floatingView)
            floatingView = null
        }
    }
}
