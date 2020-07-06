package com.arttseng.screenrecorder

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arttseng.screenrecorder.Tools.Companion.toast
import kotlinx.android.synthetic.main.activity_launch.*
import kotlinx.android.synthetic.main.activity_main.*

class LaunchActivity : AppCompatActivity() {
    val REQUEST_MEDIA_PROJECTION = 1000
    val REQUEST_WRITE_STORAGE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        setupPermissions()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_STORAGE)
        } else {
            requestRecording()
        }

    }

    lateinit var manager: MediaProjectionManager
    private fun requestRecording() {
        manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(manager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_WRITE_STORAGE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    toast("User Deny")
                } else {
                    requestRecording()
                }
            }

        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, dataIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, dataIntent)
        when(requestCode) {
            REQUEST_MEDIA_PROJECTION-> {
                toast("设定完成")
                toast("背景服务")

                val serviceClass = ServiceDemo::class.java
                val intent = Intent(applicationContext, serviceClass)
                if (!isServiceRunning(serviceClass)) {
                    MyApplication.get().putData("intent", dataIntent)
                    startService(intent)
                } else {
                    toast("Service already running.")
                }

                moveTaskToBack(true)
            }
        }
    }

    private fun isServiceRunning(serviceClass: Class<*>): Boolean {
        val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        // Loop through the running services
        for (service in activityManager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.name == service.service.className) {
                // If the service is running then return true
                return true
            }
        }
        return false
    }
}