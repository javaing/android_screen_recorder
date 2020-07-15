package com.arttseng.screenrecorder

import android.app.Service
import android.content.Intent
import android.net.Uri
import android.os.IBinder
import android.util.Log
import com.arttseng.screenrecorder.Tools.Companion.toast
import com.arttseng.screenrecorder.tools.GameData
import com.arttseng.screenrecorder.tools.RetrofitFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.timerTask


class SacnService : Service() {
    //val gameTimeList = ArrayList<String>()

    val testStr = "[\n" +
            "   {\n" +
            "      \"id\":1,\n" +
            "      \"AnchorID\":45456,\n" +
            "      \"AnchorName\":\"測試員一號\",\n" +
            "      \"LeagueName\":\"法马利卡奥-朴迪莫伦斯\",\n" +
            "      \"GameStart\":\"2020-07-15T09:20:00Z\",\n" +
            "    \"GameEnd\":\"2020-07-15T18:00:00Z\",\n" +
            "    \"Url\":\"https://smzb.io/room/70\",\n" +
            "      \"Status\":\"0\"\n" +
            "   },\n" +
            "   {\n" +
            "      \"id\":2,\n" +
            "      \"AnchorID\":45456,\n" +
            "      \"AnchorName\":\"測試員二號\",\n" +
            "      \"LeagueName\":\"法马利卡奥-朴迪莫伦斯\",\n" +
            "      \"GameStart\":\"2020-07-14T11:50:00Z\",\n" +
            "    \"GameEnd\":\"2020-07-15T18:00:00Z\",\n" +
            "    \"Url\":\"https://smzb.io/room/55\",\n" +
            "      \"Status\":\"0\"\n" +
            "   },\n" +
            "   {\n" +
            "      \"id\":3,\n" +
            "      \"AnchorID\":45456,\n" +
            "      \"AnchorName\":\"測試員三號\",\n" +
            "      \"LeagueName\":\"法马利卡奥-朴迪莫伦斯\",\n" +
            "      \"GameStart\":\"2020-07-14T18:30:00Z\",\n" +
            "    \"GameEnd\":\"2020-07-15T12:30:00Z\",\n" +
            "    \"Url\":\"https://smzb.io/room/356\",\n" +
            "      \"Status\":\"0\"\n" +
            "   }\n" +
            "]"

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        toast("背景扫描开始")

        //gameTimeList.add("2020-07-07T16:32:00Z")
        //gameTimeList.add("2020-07-08T16:35:00Z")
        //gameTimeList.add("2020-07-09T16:38:00Z")

        scan()

        return START_STICKY
    }

    private fun scan() {
        val mockData = genTestData()

//        Timer().schedule(timerTask {
//            mockData?.let { processData(mockData) }
//            //scanMatch()
//        }, 1000, Const.ScanPeriod)

        Completable.complete()
            .delay(1000, TimeUnit.MILLISECONDS)
            .repeat(Const.RecordingShift.toLong())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnComplete {
                mockData?.let { processData(mockData) }
                //scanMatch()
            }
            .subscribe()
    }

    private fun genTestData():List<GameData>? {
        val moshi = Moshi.Builder().build()
        val type = Types.newParameterizedType(List::class.java, GameData::class.java)
        val adapter = moshi.adapter<List<GameData>>(type)
        return adapter.fromJson(testStr)
    }

    //扫描到，大於开始时间的，pick它，call updateStatus API,
    //启动一个Timer for shift用，shift时间到了之后，启动录影机制，结束这个 timer
    private fun scanMatch() {
        if(Tools.isRecording) {
            Log.d("TEST", "isRecording pass Scan API")
            return
        }

        GlobalScope.launch(Dispatchers.IO) {
            val webResponse = RetrofitFactory.WebAccess.API.getGameList().await()
            if (webResponse.isSuccessful) {
                val data : List<GameData>? = webResponse.body()
                //Log.d("TEST", data?.toString())

                data?.let { processData(data) }
            } else {
                Log.d("TEST", "Error ${webResponse.code()}")
            }
        }
    }

    //check是否有录影中赛事, 状态:录影中，等待中
    //if 录影中，then pass
    //if 等待中, then 挑选还不到结束时间，且status=0的赛事
    //call UpdateStatusAPI
    //呼叫录制
    private fun processData(dataList : List<GameData>) {
        //Log.e("TEST","size="+dataList.size)
        if(dataList.isNotEmpty()) {
            Log.e("TEST", "isRecording="+Tools.isRecording)
            if(!Tools.isRecording) {
                //挑选还不到结束时间，且status=0的赛事
                dataList.forEach {
                    if(Tools.isAfterStartBeforeEnd(it)) {
                        Tools.isRecording=true
                        updateStatusAPI(it.id, it.MobileNumber?:"0000")

                        Timer().schedule(timerTask {
                            wakeupMain(it.Url?:Const.SMTV, Tools.getMatchTitle(it))
                            //captureScreen(it.Url?:Const.SMTV, Tools.getMatchTitle(it))
                        }, shiftDate(it))
                        return
                    }
                }

            }
        }
    }

    private fun shiftDate(it:GameData):Date {
        var date = Tools.matchTimeToDate(it.GameStart)
        return Tools.getShiftTime(date, Const.RecordingShift.toInt())
    }


    //url=http://203.69.207.107:9001/smzb/api/UpdateStatus?id=1&MobileNumber=0000&device=HUAWEI_MHA-L29&version=9
    private fun updateStatusAPI(id: Int , mobile: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val webResponse = RetrofitFactory.WebAccess.API.updateStatus(id, mobile, Tools.getDeviceName(), Tools.getAndroidVersion()).await()
            if (webResponse.isSuccessful) {
                Log.e("TEST","updateStatus isSuccessful")
            } else {
                Log.e("TEST", "updateStatusAPI Error ${webResponse.code()}")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //toast("背景扫描结束")
        Log.e("TEST", "背景扫描结束，重启" )
        scan()
    }


//    private fun checkGameTime():Boolean {
//        if(gameTimeList.contains(Tools.currentTimeToMinute()))
//            return true
//        else
//            Log.e("TEST", "pass:" + Tools.currentTimeToMinute())
//        return false
//    }

    //内开Webview
    private fun wakeupMain(url:String, title:String) {
        //if(checkGameTime()) {
        val intent = Intent(applicationContext, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        intent.putExtra(Const.URL, url)
        intent.putExtra(Const.Title, title)
        startActivity(intent)
        //}
    }

    //外开手机浏览器
    private fun captureScreen(url:String, title:String) {
        val resultCode = MyApplication.getData(Const.KEY_MEDIA_PROJECTION_RESULTCODE) as Int
        val intent = MyApplication.getData(Const.KEY_MEDIA_PROJECTION_INTENT) as Intent
        Tools.startRecord(this, resultCode, intent, Tools.getFilename(title))
        Timer().schedule(timerTask{
            Tools.stopRecording(Tools.mMediaRecorder, Tools.projection)
        },Const.RecordingLength)

        val openURL = Intent(Intent.ACTION_VIEW)
        openURL.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        if(url.indexOf("http")<0)
            openURL.data = Uri.parse(Const.SMTV + url)
        else
            openURL.data = Uri.parse(url)
        startActivity(openURL)
    }

}

