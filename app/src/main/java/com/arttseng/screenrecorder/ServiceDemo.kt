package com.arttseng.screenrecorder

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import com.arttseng.screenrecorder.Tools.Companion.toast
import java.util.*

class ServiceDemo : Service() {
    private lateinit var mHandler: Handler
    private lateinit var mRunnable: Runnable


    lateinit var myIntent :Intent

    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Send a notification that service is started
        toast("Service started.")

        // Do a periodic task
        mHandler = Handler()
        mRunnable = Runnable { showRandomNumber() }
        mHandler.postDelayed(mRunnable, 2000)

        return Service.START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        toast("Service destroyed.")
        //if(mHandler!=null)
            //mHandler.removeCallbacks(mRunnable)
    }

    // Custom method to do a task
    private fun showRandomNumber() {
        val rand = Random()
        val number = rand.nextInt(100)
        toast("Random Number : $number")
        mHandler.postDelayed(Runnable {
            Tools.stopRecording()
            toast("stop recording")
        }, 1000*10)

        myIntent = MyApplication.get().getData("intent") as Intent
        Tools.startRecord(applicationContext,0, myIntent)
    }



}

