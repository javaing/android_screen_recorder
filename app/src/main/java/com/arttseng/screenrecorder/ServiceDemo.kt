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

        gameTimeList.add("2020/07/06 16:31")
        gameTimeList.add("2020/07/06 16:35")

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
            intent.putExtra("url", "https://smtv.io/room/2323726/625809")
            intent.putExtra("title", "虚拟赛事 - 菲律宾 VS 土库曼 高清直播14")
            startActivity(intent)
        }

    }

}

