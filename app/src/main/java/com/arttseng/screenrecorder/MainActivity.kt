package com.arttseng.screenrecorder

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.PixelFormat
import android.media.CamcorderProfile
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.os.Environment.getExternalStorageDirectory
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.tencent.smtt.export.external.interfaces.SslError
import com.tencent.smtt.export.external.interfaces.SslErrorHandler
import com.tencent.smtt.sdk.QbSdk
import com.tencent.smtt.sdk.WebChromeClient
import com.tencent.smtt.sdk.WebView
import com.tencent.smtt.sdk.WebViewClient
import kotlinx.android.synthetic.main.activity_main.*
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {

    val REQUEST_MEDIA_PROJECTION = 1000
    val REQUEST_WRITE_STORAGE = 1001

    lateinit var manager: MediaProjectionManager
    lateinit var filename :String
    lateinit var mMediaRecorder: MediaRecorder
    lateinit var projection:MediaProjection

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getWindow().setFormat(PixelFormat.TRANSLUCENT)
        setContentView(R.layout.activity_main)

//        val webSettings = webView.getSettings()
//        webSettings.javaScriptEnabled
//        //webSettings.domStorageEnabled
//        //webSettings.setAppCacheEnabled(true)
//        //webView.clearCache(true)
//        //webView.clearHistory()
//        webView.webViewClient = object: WebViewClient() {
//
//            override fun onReceivedSslError(view: WebView,
//                                            handler: SslErrorHandler, error:SslError) {
//                super.onReceivedSslError(view, handler, error)
//                handler.proceed()
//        }
//		}



        //webView.loadUrl("https://104.22.1.70")
        //webView.loadUrl("https://smtv.io")
        webView2.loadUrl("https://www.pchome.com.tw/")



        tv_stop.setOnClickListener {
            mMediaRecorder?.stop()
            projection?.stop()
        }


        //filename = this.getExternalFilesDir("")?.absolutePath+"mobile01.mp4"
        //setupPermissions()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            Log.i("ART", "Permission to record denied")
            ActivityCompat.requestPermissions(this,
                arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_STORAGE)
        } else {
            startRecord(0, Intent())
        }

    }

    private fun requestRecording() {
        startActivityForResult(manager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
        Log.e("ART", "01")
    }


    private fun startRecord(resultCode: Int, data: Intent) {
Log.e("ART", "1")
        manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        if(manager==null) {
            return
        }
        projection = manager.getMediaProjection(resultCode, data)
        Log.e("ART", "2")
        val metrics = getResources().getDisplayMetrics()
        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT)
        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        val profile: CamcorderProfile = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH)
        profile.videoFrameHeight = metrics.heightPixels
        profile.videoFrameWidth = metrics.widthPixels
        mMediaRecorder.setProfile(profile)
        mMediaRecorder.setOutputFile(filename)
        mMediaRecorder.prepare()
        mMediaRecorder.start()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
//            REQUEST_WRITE_STORAGE-> {
//                //requestRecording()
//                Toast.makeText(this, "1", Toast.LENGTH_SHORT).show()
//            }

            REQUEST_MEDIA_PROJECTION-> {
                startRecord(resultCode, data!!)
                //Toast.makeText(this, "2", Toast.LENGTH_SHORT).show()
            }
        }
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
//            REQUEST_MEDIA_PROJECTION -> {
//
//                if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//
//                    Toast.makeText(this, "User Deny", Toast.LENGTH_SHORT).show()
//                } else {
//                    startRecord(0, Intent())
//                }
//            }

        }
    }





}
