package com.arttseng.screenrecorder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaRecorder
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.os.Handler
import android.os.PowerManager
import android.util.Log
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import com.arttseng.screenrecorder.Tools.Companion.toast
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask


class MainActivity : AppCompatActivity() {

    val REQUEST_MEDIA_PROJECTION = 1000
    lateinit var filename: String

    @SuppressLint("InvalidWakeLockTag")
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

        webView.loadUrl(intent.getStringExtra(Consts.URL))
        val title = intent.getStringExtra(Consts.Title).replace("-","_").replace(" ", "")

        filename = Tools.getFilename(title)
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
        if (MyApplication.getData(Consts.KEY_MEDIA_PROJECTION_INTENT)==null) {
            startActivityForResult(manager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
        } else {
            captureScreen();
        }
    }

    private fun captureScreen() {
        val resultCode = MyApplication.getData(Consts.KEY_MEDIA_PROJECTION_RESULTCODE) as Int
        val intent = MyApplication.getData(Consts.KEY_MEDIA_PROJECTION_INTENT) as Intent
        val projection = manager?.getMediaProjection(resultCode, intent)
        val recorder = MediaRecorder()
        Tools.startRecord2(this,recorder, filename, projection)
        Timer().schedule(timerTask{
            Tools.stopRecording()
            toast("stop recording")
            webView.loadData("<HTML><BODY><H3>Test</H3></BODY></HTML>","text/html","utf-8");
            moveTaskToBack(true)
        },Consts.RecordingLength)
    }


    lateinit var manager: MediaProjectionManager
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_MEDIA_PROJECTION-> {
                MyApplication.putData(Consts.KEY_MEDIA_PROJECTION_RESULTCODE, resultCode)
                MyApplication.putData(Consts.KEY_MEDIA_PROJECTION_INTENT, data)
                captureScreen()
            }
        }
    }

}
