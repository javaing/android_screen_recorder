package com.arttseng.screenrecorder

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.webkit.WebSettings
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    val REQUEST_MEDIA_PROJECTION = 1000
    val REQUEST_WRITE_STORAGE = 1001



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getWindow().setFormat(PixelFormat.TRANSLUCENT)
        setContentView(R.layout.activity_main)

        val webSettings = webView.settings
        val appCachePath: String = this.applicationContext.cacheDir.absolutePath

        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.setAppCachePath(appCachePath)
        webSettings.setAppCacheEnabled(false)
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE


        webView.loadUrl("https://smtv.io")


        updateStatus("运行")
        tv_stop.setOnClickListener {
            Tools.stopRecording()
            updateStatus("结束录制")
            tv_stop.text = "Done"
        }

//        mMediaRecorder = MediaRecorder()
//        filename = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path +"/mobile01.mp4"
//        Log.e("TEST", filename)
        setupPermissions()
    }


    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("TEST", "Permission to record denied")
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
        Log.e("TEST", "01")
    }


    private fun updateStatus(status: String) {
        tv_status.text = status
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, dataIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, dataIntent)
        when(requestCode) {
//            REQUEST_WRITE_STORAGE-> {
//                //requestRecording()
//                Toast.makeText(this, "1", Toast.LENGTH_SHORT).show()
//            }

            REQUEST_MEDIA_PROJECTION-> {
                Tools.startRecord(this, resultCode, dataIntent!!)
                updateStatus("录制中")
                tv_stop.text = "Stop"
                tv_stop.visibility = View.VISIBLE

                //toast("Start Service")
//                val serviceClass = ServiceDemo::class.java
//                val intent = Intent(applicationContext, serviceClass)
//                if (!isServiceRunning(serviceClass)) {
//                    MyApplication.get().putData("intent", dataIntent)
//                    startService(intent)
//                } else {
//                    toast("Service already running.")
//                }



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
    fun Context.toast(message:String){
        Toast.makeText(applicationContext,message,Toast.LENGTH_SHORT).show()
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        when (requestCode) {
            REQUEST_WRITE_STORAGE -> {
                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "User Deny", Toast.LENGTH_SHORT).show()
                } else {
                    requestRecording()
                }
            }

        }
    }





}
