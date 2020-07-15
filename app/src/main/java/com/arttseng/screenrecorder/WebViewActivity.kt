package com.arttseng.screenrecorder

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.media.MediaRecorder
import android.media.projection.MediaProjectionManager
import android.os.*
import android.util.Log
import android.webkit.WebSettings
import androidx.appcompat.app.AppCompatActivity
import com.arttseng.screenrecorder.Tools.Companion.toast
import kotlinx.android.synthetic.main.activity_main.*


class WebViewActivity : AppCompatActivity() {

    val REQUEST_MEDIA_PROJECTION = 1000
    lateinit var filename: String

    @SuppressLint("InvalidWakeLockTag")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        turnOnStrictMode()
//        permitDiskReads{
//            super.onCreate(savedInstanceState)
//        }

        getWindow().setFormat(PixelFormat.TRANSLUCENT)
        setContentView(R.layout.activity_main)

        emptyWebView()

        val webSettings = webView.settings
        val appCachePath: String = this.applicationContext.cacheDir.absolutePath

        webSettings.javaScriptEnabled = true
        webSettings.domStorageEnabled = true
        webSettings.allowFileAccess = true
        webSettings.setAppCachePath(appCachePath)
        webSettings.setAppCacheEnabled(false)
        webSettings.cacheMode = WebSettings.LOAD_NO_CACHE

        webView.loadUrl(intent.getStringExtra(Const.URL))
        val title = intent.getStringExtra(Const.Title).replace("-","_").replace(" ", "")

        filename = Tools.getFilename(title)
        Log.e("TEST", "WebView path=" + filename)

        val screenLock =
            (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(
                PowerManager.SCREEN_BRIGHT_WAKE_LOCK or PowerManager.ACQUIRE_CAUSES_WAKEUP, "TAG"
            )
        screenLock.acquire()

        requestRecording()
    }


    private fun requestRecording() {
        manager = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
        if (MyApplication.getData(Const.KEY_MEDIA_PROJECTION_INTENT)==null) {
            startActivityForResult(manager.createScreenCaptureIntent(), REQUEST_MEDIA_PROJECTION)
        } else {
            captureScreen();
        }
    }

    private fun emptyWebView() {
        webView.loadData("<HTML><BODY><H3>AutoRecording Webview</H3></BODY></HTML>","text/html","utf-8");
    }


    private fun captureScreen() {
        val resultCode = MyApplication.getData(Const.KEY_MEDIA_PROJECTION_RESULTCODE) as Int
        val intent = MyApplication.getData(Const.KEY_MEDIA_PROJECTION_INTENT) as Intent
        val projection = manager?.getMediaProjection(resultCode, intent)
        val recorder = MediaRecorder()
        Tools.startRecord2(this, recorder, filename, projection)

        Handler().postDelayed({
            Tools.stopRecording(recorder, projection)
            //emptyWebView()
            toast("录影结束")
            Log.e("TEST", "录影结束" )
            Log.e("TEST", "isRecording="+Tools.isRecording)
            //moveTaskToBack(true)
        },Const.RecordingLength)
    }


    lateinit var manager: MediaProjectionManager
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when(requestCode) {
            REQUEST_MEDIA_PROJECTION-> {
                MyApplication.putData(Const.KEY_MEDIA_PROJECTION_RESULTCODE, resultCode)
                MyApplication.putData(Const.KEY_MEDIA_PROJECTION_INTENT, data)
                captureScreen()
            }
        }
    }

}
