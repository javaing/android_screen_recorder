package com.arttseng.screenrecorder

import android.annotation.SuppressLint
import android.app.KeyguardManager
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.view.Window
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import com.arttseng.screenrecorder.Tools.Companion.toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File


class MainActivity : AppCompatActivity() {
    val REQUEST_MEDIA_PROJECTION = 1000
    lateinit var filename: String
    private lateinit var mHandler: Handler

    @SuppressLint("InvalidWakeLockTag")
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
        filename = "/storage/emulated/0/Pictures/screenshots" + File.separator  + title +".mp4"

        Log.e("TEST", filename)

        val screenLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG"
            )
        screenLock.acquire()

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
                    webView.loadData("<HTML><BODY><H3>Test</H3></BODY></HTML>","text/html","utf-8");
                    moveTaskToBack(true)
                }, 1000*60)
            }
        }
    }



}
