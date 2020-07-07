package com.arttseng.screenrecorder

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.arttseng.screenrecorder.Tools.Companion.toast
import org.json.JSONObject
import java.util.*
import kotlin.concurrent.timerTask

class ServiceDemo : Service() {
   //yyyyMMdd HH:mm
    val gameTimeList = ArrayList<String>()
    var isRecording  = false

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        toast("Service started.")

        gameTimeList.add("2020/07/07 11:05")
        gameTimeList.add("2020/07/07 11:10")

        //Log.e("TEST", "service ScanPeriod="+ Consts.ScanPeriod)
        //Log.e("TEST", "service RecordingLength=" + Consts.RecordingLength)

        Timer().schedule(timerTask {
            //callMatchAPI()
            captureScreen("https://smtv.io/room/2305228", "米儿")
        },1000, Consts.ScanPeriod) //check API



        return START_STICKY
    }

    private fun callMatchAPI() {
        Tools.httpGet(Consts.MatchAPI, object:SolarCallBack{
            override fun onErr(errorMsg: String?) {
                //check是否有录影中赛事, 状态:录影中，等待中
                //if 录影中，then pass
                //if 等待中, then 挑选还不到结束时间，且status=0的赛事
                //call UpdateStatusAPI
                //呼叫录制
                if(!isRecording) {
                    //挑选还不到结束时间，且status=0的赛事
                    callUpdateStatusAPI()
                    //wakeupMain("https://smtv.io/room/2305228", "米儿")
                    captureScreen("https://smtv.io/room/2305228", "米儿")
                    isRecording=true
                    Handler().postDelayed({
                        isRecording=false
                    }, Consts.RecordingLength)
                }
            }

            override fun onOK(jsonObject: JSONObject?) {

            }

        })
    }

    private fun callUpdateStatusAPI() {
        Tools.httpGet(Consts.UpdateStatusAPI, object:SolarCallBack{
            override fun onErr(errorMsg: String?) {}
            override fun onOK(jsonObject: JSONObject?) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        toast("Service destroyed.")
    }


    private fun checkGameTime():Boolean {
        if(gameTimeList.contains(Tools.currentTimeToMinute()))
            return true
        else
            Log.e("TEST", "pass:" + Tools.currentTimeToMinute())
        return false
    }

    //内开Webview
    private fun wakeupMain(url:String, title:String) {
        if(checkGameTime()) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Consts.URL, url)
            intent.putExtra(Consts.Title, title)
            startActivity(intent)
        }
    }

    //外开手机浏览器
    private fun captureScreen(url:String, title:String) {
        val resultCode = MyApplication.getData(Consts.KEY_MEDIA_PROJECTION_RESULTCODE) as Int
        val intent = MyApplication.getData(Consts.KEY_MEDIA_PROJECTION_INTENT) as Intent
        Tools.startRecord(this, resultCode, intent, Tools.getFilename(title))
        Timer().schedule(timerTask{
            Tools.stopRecording()
        },Consts.RecordingLength)

        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        openURL.data = Uri.parse(url)
        startActivity(openURL)
    }

}

