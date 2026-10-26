package com.gamekeypro.ui
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.gamekeypro.R
import com.gamekeypro.engine.TouchInjector
import com.gamekeypro.service.OverlayService
import rikka.shizuku.Shizuku

class MainActivity : AppCompatActivity() {
    private val SHIZUKU_CODE = 4001
    private var isServiceRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btnGrantOverlay).setOnClickListener {
            if (!Settings.canDrawOverlays(this)) {
                startActivity(Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:$packageName")))
            } else Toast.makeText(this, "Overlay already granted", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnGrantShizuku).setOnClickListener {
            if (Shizuku.pingBinder()) Shizuku.requestPermission(SHIZUKU_CODE)
            else Toast.makeText(this, "Start Shizuku first", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.btnToggleService).setOnClickListener {
            val serviceIntent = Intent(this, OverlayService::class.java)
            if (!isServiceRunning) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) startForegroundService(serviceIntent) else startService(serviceIntent)
                isServiceRunning = true
            } else {
                stopService(serviceIntent)
                isServiceRunning = false
            }
        }
    }
}
