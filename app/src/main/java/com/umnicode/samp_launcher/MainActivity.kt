package com.umnicode.samp_launcher

import android.Manifest
import android.content.pm.ActivityInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

class PermissionRequest(val Permission: String, var Callbacks: MutableList<PermissionRequestCallback>) {}

class MainActivity : AppCompatActivity() {
    private lateinit var PermissionRequests: MutableList<PermissionRequest>
    private var PermissionRequestID: Int = 0

    override fun onCreate(savedInstanceStatus: Bundle?) {
        super.onCreate(savedInstanceStatus)

        this.PermissionRequests = mutableListOf()

        super.requestWindowFeature(Window.FEATURE_NO_TITLE)
        super.getSupportActionBar()?.hide()

        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE

        setContentView(R.layout.activity_main)

        // Обработка кнопок
        findViewById<Button>(R.id.button_play).setOnClickListener {
            Toast.makeText(this, "Запуск игры...", Toast.LENGTH_SHORT).show()
        }

        findViewById<Button>(R.id.button_settings).setOnClickListener {
            Toast.makeText(this, "Открываем настройки...", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            (this.applicationContext as LauncherApplication).Installer.ReCheckInstallResources(this)
        }
    }

    private fun FindPermissionRequest(Permission: String): PermissionRequest? {
        for (LPermission in this.PermissionRequests) {
            if (LPermission.Permission == Permission) return LPermission
        }
        return null
    }

    fun RequestPermission(Permission: String, CallbackResult: PermissionRequestCallback) {
        if (ActivityCompat.checkSelfPermission(this, Permission) == PackageManager.PERMISSION_GRANTED) {
            CallbackResult.Finished(true)
            return
        }

        val PermissionReq: PermissionRequest? = this.FindPermissionRequest(Permission)

        if (PermissionReq != null) {
            PermissionReq.Callbacks.add(CallbackResult)
        } else {
            this.PermissionRequests.add(PermissionRequest(Permission, mutableListOf(CallbackResult)))
            ActivityCompat.requestPermissions(this, arrayOf(Permission), this.PermissionRequestID)
            this.PermissionRequestID++
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        for (i in permissions.indices) {
            for (Index in PermissionRequests.indices) {
                if (PermissionRequests[Index].Permission == permissions[i]) {
                    for (Callback in PermissionRequests[Index].Callbacks) {
                        Callback.Finished(grantResults[i] == PackageManager.PERMISSION_GRANTED)
                    }
                    this.PermissionRequests.removeAt(Index)
                    break
                }
            }
        }
    }

    // Эти методы теперь публичные и доступны для Java-класса SAMPInstaller
    fun IsStorageReadPermissionsGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun IsStorageWritePermissionsGranted(): Boolean {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    fun IsStoragePermissionsGranted(): Boolean {
        return this.IsStorageReadPermissionsGranted() && this.IsStorageWritePermissionsGranted()
    }
}