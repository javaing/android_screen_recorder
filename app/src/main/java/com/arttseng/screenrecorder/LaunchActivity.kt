package com.arttseng.screenrecorder

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.arttseng.screenrecorder.Tools.Companion.toast
import kotlinx.android.synthetic.main.activity_launch.*

class LaunchActivity : AppCompatActivity() {
    val REQUEST_MEDIA_PROJECTION = 1000
    val REQUEST_PERMISSION = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_launch)

        val adapter1 = ArrayAdapter.createFromResource(this, R.array.scan_period, android.R.layout.simple_spinner_dropdown_item)
        spinner_scan.adapter = adapter1
        spinner_scan.setSelection(Tools.readData(this,"scan_period", 1))

        val adapter2 = ArrayAdapter.createFromResource(this, R.array.recording_length, android.R.layout.simple_spinner_dropdown_item)
        spinner_recording.adapter = adapter2
        spinner_recording.setSelection(Tools.readData(this,"recording_length", 1))

        et_baseurl.setText( Tools.readData(this,"BaseURL", Const.BaseURL)?:Const.BaseURL)
        et_savepath.setText( Tools.readData(this,"SavePath", Const.SavePath)?:Const.SavePath)
        et_shift.setText( Tools.readData(this,"RecordingShift", Const.RecordingShift)?:Const.RecordingShift)


        tv_start.setOnClickListener {
            Tools.saveData(this,"scan_period", spinner_scan.selectedItemPosition)
            Tools.saveData(this,"recording_length", spinner_recording.selectedItemPosition)
            Tools.saveData(this,"BaseURL", et_baseurl.text.toString())
            Tools.saveData(this,"SavePath", et_savepath.text.toString())
            Tools.saveData(this,"RecordingShift", et_shift.text.toString())

            val recording = resources.getIntArray(R.array.recording_length_minute)
            val scan = resources.getIntArray(R.array.scan_period_minute)
            Const.ScanPeriod = Tools.minuteToLong( scan[spinner_scan.selectedItemPosition] )
            Const.RecordingLength = Tools.minuteToLong( recording[spinner_recording.selectedItemPosition] )
            Const.BaseURL = et_baseurl.text.toString()
            Const.SavePath = et_savepath.text.toString()
            Const.RecordingShift = et_shift.text.toString()

           //Log.e("TEST", "SET ="+Const.ScanPeriod)
            //Log.e("TEST", "SET ="+Const.RecordingLength)
            //Log.e("TEST", "SET ="+Const.RecordingShift)

            setupPermissions()
        }


    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    android.Manifest.permission.RECORD_AUDIO,
                    android.Manifest.permission.WAKE_LOCK),
                REQUEST_PERMISSION)
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
            REQUEST_PERMISSION -> {
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

                val serviceClass = SacnService::class.java
                val intent = Intent(applicationContext, serviceClass)
                if (!isServiceRunning(serviceClass)) {
                    MyApplication.putData(Const.KEY_MEDIA_PROJECTION_RESULTCODE, resultCode);
                    MyApplication.putData(Const.KEY_MEDIA_PROJECTION_INTENT, dataIntent);
                    toast("背景执行中")
                    intent.putExtra("code", resultCode);
                    intent.putExtra("data", dataIntent);
                    startService(intent)
                    moveTaskToBack(true)

                } else {
                    toast("背景已执行")
                }


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