package com.arttseng.screenrecorder

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import com.arttseng.screenrecorder.Tools.Companion.toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {
    val REQUEST_MEDIA_PROJECTION = 1000
    lateinit var filename: String
    private lateinit var mHandler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        getWindow().setFormat(PixelFormat.TRANSLUCENT)
        setContentView(R.layout.activity_main)

        mHandler = Handler()

        val webSettings = webView.settings
        val appCachePath: String = this.applicationContext.cacheDir.absolutePath

        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.setAppCachePath(appCachePath)
        webSettings.setAppCacheEnabled(false)
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE


        webView.loadUrl(intent.getStringExtra("url"))
        val title = intent.getStringExtra("title").replace("-","_").replace(" ", "")
        filename = getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path + File.separator  + title +".mp4"
        Log.e("TEST", filename)

        requestRecording()
    }

    private fun requestRecording() {
        manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        startActivityForResult(manager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
    }


    lateinit var manager: MediaProjectionManager
    override fun onActivityResult(requestCode: Int, resultCode: Int, dataIntent: Intent?) {
        super.onActivityResult(requestCode, resultCode, dataIntent)
        when(requestCode) {
            REQUEST_MEDIA_PROJECTION-> {
                Tools.startRecord(this, resultCode, dataIntent!!, filename)
                mHandler.postDelayed(Runnable {
                    Tools.stopRecording()
                    toast("stop recording")
                    webView.loadUrl("")
                    moveTaskToBack(true)
                }, 1000*60)
            }
        }
    }






}
