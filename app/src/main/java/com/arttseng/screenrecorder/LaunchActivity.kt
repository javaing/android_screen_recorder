package com.arttseng.screenrecorder

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.media.projection.MediaProjectionManager
import android.os.Bundle
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

        val adapter1 = ArrayAdapter.createFromResource(this, R.array.recording_length, android.R.layout.simple_spinner_dropdown_item)
        spinner1.adapter = adapter1
        spinner1.setSelection(Tools.readData(this,"recording_length", 1))

        val adapter2 = ArrayAdapter.createFromResource(this, R.array.scan_period, android.R.layout.simple_spinner_dropdown_item)
        spinner2.adapter = adapter2
        spinner2.setSelection(Tools.readData(this,"scan_period", 1))

        tv_start.setOnClickListener {
            Tools.saveData(this,"scan_period", spinner1.selectedItemPosition)
            Tools.saveData(this,"recording_length", spinner2.selectedItemPosition)

            val recording = resources.getIntArray(R.array.recording_length_minute)
            val scan = resources.getIntArray(R.array.scan_period_minute)
            Consts.ScanPeriod = Tools.minuteToLong( recording[spinner1.selectedItemPosition] )
            Consts.RecordingLength = Tools.minuteToLong( scan[spinner2.selectedItemPosition] )

            setupPermissions()
        }

        tv_savePath.text = Consts.VideoPath
        tv_api1.text = Consts.MatchAPI
        tv_api2.text = Consts.UpdateStatusAPI

        tv_more.setOnClickListener {
            if(ll_more.visibility== View.VISIBLE) {
                tv_more.setTextColor(Color.GRAY)
                ll_more.visibility = View.INVISIBLE
            } else {
                tv_more.setTextColor(Color.BLUE)
                ll_more.visibility = View.VISIBLE
            }
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
                    MyApplication.putData(Consts.KEY_MEDIA_PROJECTION_RESULTCODE, resultCode);
                    MyApplication.putData(Consts.KEY_MEDIA_PROJECTION_INTENT, dataIntent);
                    toast("背景服务")
                    startService(intent)

//                    val intent = Intent(this, MainActivity::class.java)
//                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    intent.putExtra("url", "https://smtv.io/room/2334813")
//                    intent.putExtra("title", "电竞足球-职业联赛-12分钟比赛 - 爸爸克里克(ROB)电竞 VS 赫塔费罗尼亚(CFC)电竞 高清直播47")
//                    startActivity(intent)

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