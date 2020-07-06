package com.arttseng.screenrecorder

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.util.Log
import com.arttseng.screenrecorder.Tools.Companion.toast
import java.util.*
import kotlin.concurrent.timerTask

class ServiceDemo : Service() {
   //yyyyMMdd HH:mm
    val gameTimeList = ArrayList<String>()


    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        toast("Service started.")

        gameTimeList.add("2020/07/06 16:50")
        gameTimeList.add("2020/07/06 16:55")

        Timer().schedule(timerTask {
            wakeupMain()
        },1000, 60000)



        return Service.START_STICKY
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

    // Custom method to do a task
    private fun wakeupMain() {
        if(checkGameTime()) {
            val intent = Intent(applicationContext, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("url", "https://smtv.io")
            intent.putExtra("title", "电竞足球-职业联赛-12分钟比赛 - 爸爸克里克(ROB)电竞 VS 赫塔费罗尼亚(CFC)电竞 高清直播47")
            startActivity(intent)
        }

    }

}

