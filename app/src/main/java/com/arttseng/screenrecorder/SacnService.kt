package com.arttseng.screenrecorder

import com.arttseng.screenrecorder.model.MatchInfo
import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.arttseng.screenrecorder.Tools.Companion.toast
import com.google.gson.Gson
import java.util.*
import kotlin.concurrent.timerTask

class SacnService : Service() {
   //yyyyMMdd HH:mm
    val gameTimeList = ArrayList<String>()
    var isRecording  = false


    val testStr = "[\n" +
            "   {\n" +
            "      \"id\":1,\n" +
            "      \"AnchorID\":45456,\n" +
            "      \"AnchorName\":\"測試員一號\",\n" +
            "      \"LeagueName\":\"法马利卡奥-朴迪莫伦斯\",\n" +
            "      \"GameStart\":\"2020-07-07T18:00:00Z\",\n" +
            "    \"GameEnd\":\"2020-07-07T18:00:00Z\",\n" +
            "    \"Url\":\"https://smzb.io/room/70\",\n" +
            "      \"Status\":\"0\"\n" +
            "   },\n" +
            "   {\n" +
            "      \"id\":2,\n" +
            "      \"AnchorID\":45456,\n" +
            "      \"AnchorName\":\"測試員二號\",\n" +
            "      \"LeagueName\":\"法马利卡奥-朴迪莫伦斯\",\n" +
            "      \"GameStart\":\"2020-07-07T18:00:00Z\",\n" +
            "    \"GameEnd\":\"2020-07-08T12:00:00Z\",\n" +
            "    \"Url\":\"https://smzb.io/room/55\",\n" +
            "      \"Status\":\"0\"\n" +
            "   },\n" +
            "   {\n" +
            "      \"id\":3,\n" +
            "      \"AnchorID\":45456,\n" +
            "      \"AnchorName\":\"測試員三號\",\n" +
            "      \"LeagueName\":\"法马利卡奥-朴迪莫伦斯\",\n" +
            "      \"GameStart\":\"2020-07-07T18:00:00Z\",\n" +
            "    \"GameEnd\":\"2020-07-07T18:00:00Z\",\n" +
            "    \"Url\":\"https://smzb.io/room/356\",\n" +
            "      \"Status\":\"0\"\n" +
            "   }\n" +
            "]"

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        toast("Service started.")

        gameTimeList.add("2020-07-07T16:32:00Z")
        gameTimeList.add("2020-07-08T16:35:00Z")
        gameTimeList.add("2020-07-09T16:38:00Z")

        //Log.e("TEST", "time1=" + Tools.isBeforeGameEnd(gameTimeList[0]))
        //.e("TEST", "time1=" + Tools.isBeforeGameEnd(gameTimeList[1]))
        //Log.e("TEST", "time1=" + Tools.isBeforeGameEnd(gameTimeList[2]))

        Timer().schedule(timerTask {
            //callMatchAPI()
            //if(checkGameTime())
                //wakeupMain("https://smtv.io/room/2305228", "米儿")

            mainLogic(testStr)
        },1000, Consts.ScanPeriod) //check API



        return START_STICKY
    }

    private fun callMatchAPI() {
        Tools.httpGet(Consts.MatchAPI, object:SolarCallBack{
            override fun onOK(jsonStr: String) {
                mainLogic(jsonStr)
            }
            override fun onErr(errorMsg: String?) {
            }

        })
    }

    //check是否有录影中赛事, 状态:录影中，等待中
    //if 录影中，then pass
    //if 等待中, then 挑选还不到结束时间，且status=0的赛事
    //call UpdateStatusAPI
    //呼叫录制
    private fun mainLogic(str: String) {
        val matchList = Gson().fromJson(str, Array<MatchInfo>::class.java)
        if(matchList.size>0) {
            if(!isRecording) {
                //挑选还不到结束时间，且status=0的赛事
                matchList.forEach {
                    if(Tools.isAfterStartBeforeEnd(it)) {
                        callUpdateStatusAPI()
                        //wakeupMain("https://smtv.io/room/2305228", "米儿")
                        captureScreen(it.url, Tools.getMatchTitle(it))
                        isRecording=true
                        Handler().postDelayed({
                            isRecording=false
                            Log.e("TEST", "state isRecording=" + isRecording)
                        }, Consts.RecordingLength)
                        return@forEach
                    }
                }

            }
        }
    }



    private fun callUpdateStatusAPI() {
        Tools.httpGet(Consts.UpdateStatusAPI, object:SolarCallBack{
            override fun onErr(errorMsg: String?) {}
            override fun onOK(str: String) {}
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

