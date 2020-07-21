package com.arttseng.screenrecorder

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.DisplayMetrics
import android.util.Log
import android.widget.Toast
import com.arttseng.screenrecorder.tools.GameData
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class Tools {
    companion object {
        val solarDatePattern = "yyyy-MM-ddHH:mm:ss"
        val solarDateFormat = SimpleDateFormat(solarDatePattern)

        lateinit var manager: MediaProjectionManager
        lateinit var mMediaRecorder: MediaRecorder
        lateinit var projection: MediaProjection
        lateinit var virtualDisplay: VirtualDisplay
        var isRecording  = false

        fun isXiaomi():Boolean {
            return Build.MANUFACTURER.contains("Xiaomi")
        }

        fun startRecord(ctx:Context, resultCode: Int, data: Intent, filename: String) {
            isRecording = true
            mMediaRecorder = MediaRecorder()
            manager = ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

            projection = manager.getMediaProjection(resultCode, data)
            startRecord(ctx, filename, mMediaRecorder, projection)
            Log.e("TEST", "startRecord:" + currentTimeToMinute())
        }

        //小米会死在setVideoSize(width, height)
        //开始录制时有media server died的error
        //先以写死长宽的方式解决
        fun startRecord(ctx:Context, filename: String, outterRecorder: MediaRecorder, projection: MediaProjection) {
            val metrics = ctx.resources.displayMetrics
            var width = metrics.widthPixels
            var height = metrics.heightPixels
            if(isXiaomi()) {
                width = 1080
                height = 1920
            }
            outterRecorder?.apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setVideoSource(MediaRecorder.VideoSource.SURFACE)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setVideoEncoder(MediaRecorder.VideoEncoder.H264)
                setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
                setOutputFile(filename)
                setVideoSize(width, height)
                setVideoEncodingBitRate(10000000)
                setVideoFrameRate(30)
                try {
                    prepare()
                    virtualDisplay = projection.createVirtualDisplay("ScreenRecording",
                        width, height, metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                        outterRecorder.surface, null, null);
                } catch (e: IOException) {
                    e.printStackTrace()
                }
                start()
            }
            Log.e("TEST", "startRecord:" + currentTimeToMinute())
        }

        fun stopRecording(recorder: MediaRecorder, projection: MediaProjection) {
            isRecording = false
            recorder.setOnErrorListener(null)
            recorder.setOnInfoListener(null)
            recorder.setPreviewDisplay(null)
            try {
                recorder.stop()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            projection.stop()

            Log.e("TEST","stopRecording done.")
        }

        fun Context.toast(message:String){
            Toast.makeText(applicationContext,message, Toast.LENGTH_SHORT).show()
        }

        private fun createPath() {
            val pathFile = File(Const.SavePath)
            if(!pathFile.exists()) {
                val success = pathFile.mkdirs()
                if (success) Log.e("TEST","Directory path was created successfully")
                else         Log.e("TEST","Failed to create directory path")
            }
        }

        fun getFilename(title:String):String {
            createPath()
            return Const.SavePath + File.separator  + title +".mp4"
        }

        fun convertLongToTime(time: Long): String {
            val date = Date(time)
            return solarDateFormat.format(date)
        }

        fun currentTimeToLong(): Long {
            return System.currentTimeMillis()
        }

        fun currentTimeToMinute(): String {
            return convertLongToTime(System.currentTimeMillis())
        }

        fun convertDateToLong(date: String): Long {
            return solarDateFormat.parse(date).time
        }

        fun convertDateToDate(date: String): Date {
            return solarDateFormat.parse(date)
        }

        fun getShiftTime(date:Date, shift:Int):Date {
            val cal = Calendar.getInstance() // creates calendar
            cal.time = date // sets calendar time/date
            cal.add(Calendar.MINUTE, shift) // adds one hour
            return cal.time
        }


        private const val DATA = "PREF_DATA"
        fun readData(ctx: Context, key:String, defaultInt: Int):Int {
            val settings = ctx.getSharedPreferences(DATA, 0)
           return settings.getInt(key, defaultInt)
        }

        fun saveData(ctx: Context, key:String, value: Int) {
            val settings = ctx.getSharedPreferences(DATA, 0)
            settings.edit()
                .putInt(key,value)
                .apply()
        }

        fun readData(ctx: Context, key:String, default: String): String? {
            val settings = ctx.getSharedPreferences(DATA, 0)
            return settings.getString(key, default)
        }

        fun saveData(ctx: Context, key:String, value: String) {
            val settings = ctx.getSharedPreferences(DATA, 0)
            settings.edit()
                .putString(key,value)
                .apply()
        }

        fun minuteToLong(minute: Int):Long {
            return  minute*1000*60L
        }

        fun getDeviceName():String {
            return Build.MANUFACTURER + "_"+Build.MODEL // returns model name
        }

        fun getAndroidVersion():String {
            return Build.VERSION.RELEASE // returns model name
        }

        fun getMatchTitle(match: GameData):String {
            return match.LeagueName + "_" + match.GameStart.replace("-","").replace(":","")
        }

        fun isAfterStartBeforeEnd(it: GameData):Boolean {
            if (it.Status!=0)
                return false
            //Log.e("TEST", "it=" + it.id + "," + it.GameStart+ "," + it.GameEnd)
            val start = matchTimeToLong( it.GameStart)
            val end = matchTimeToLong(it.GameEnd?:"2030-01-01T00:00:00Z")
            val current = currentTimeToLong()
            //Log.e("TEST", "result=" + (current in (start + 1) until end))
            return current in (start + 1) until end
        }

        fun matchTimeToLong(time: String):Long {
            if(time.equals("null")) {
                return 0
            }
            // "GameEnd":"2020-07-07T18:00:00Z",
            val timeFilter = time.replace("T","").replace("Z","")
            return convertDateToLong(timeFilter)
        }

        fun matchTimeToDate(time: String):Date {
            if(time.equals("null")) {
                return Date()
            }
            // "GameEnd":"2020-07-07T18:00:00Z",
            val timeFilter = time.replace("T","").replace("Z","")
            return convertDateToDate(timeFilter)
        }

        fun dpToPx(ctx: Context, dp:Int): Float {
            val metrics = DisplayMetrics()
            (ctx as Activity).getWindowManager().getDefaultDisplay().getMetrics(metrics)
            val logicalDensity: Float = metrics.density
            return Math.ceil((dp * logicalDensity).toDouble()).toFloat()
        }
    }
}