package com.arttseng.screenrecorder

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
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

        lateinit var manager: MediaProjectionManager
        lateinit var mMediaRecorder: MediaRecorder
        lateinit var projection: MediaProjection
        lateinit var virtualDisplay: VirtualDisplay
        var isRecording  = false

        fun startRecord(ctx:Context, resultCode: Int, data: Intent, filename: String) {
            isRecording = true
            mMediaRecorder = MediaRecorder()
            manager = ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

            projection = manager.getMediaProjection(resultCode, data)
            setUpMediaRecorder(ctx, filename)
            val metrics = ctx.getResources().getDisplayMetrics()
            virtualDisplay = projection.createVirtualDisplay("ScreenRecording",
                metrics.widthPixels, metrics.heightPixels, metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null, null);

            mMediaRecorder.start()
            Log.e("TEST", "startRecord:" + currentTimeToMinute())
        }


        fun startRecord2(ctx:Context, outterRecorder: MediaRecorder, filename: String, projection: MediaProjection) {
            setUpMediaRecorder2(outterRecorder, ctx, filename)
            val metrics = ctx.getResources().getDisplayMetrics()
            virtualDisplay = projection.createVirtualDisplay("ScreenRecording",
                metrics.widthPixels, metrics.heightPixels, metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                outterRecorder.getSurface(), null, null);

            outterRecorder.start()
            Log.e("TEST", "startRecord2:" + currentTimeToMinute())
        }

        private fun setUpMediaRecorder2(outterRecorder: MediaRecorder,ctx: Context, filename: String) {
            val metrics = ctx.getResources().getDisplayMetrics()
            outterRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            outterRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            outterRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            outterRecorder.setOutputFile(filename)
            outterRecorder.setVideoEncodingBitRate(10000000)
            outterRecorder.setVideoFrameRate(30)
            outterRecorder.setVideoSize(metrics.widthPixels, metrics.heightPixels)
            outterRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            outterRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                outterRecorder.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }

        private fun setUpMediaRecorder(ctx: Context, filename: String) {
            val metrics = ctx.getResources().getDisplayMetrics()
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            mMediaRecorder.setOutputFile(filename)
            mMediaRecorder.setVideoEncodingBitRate(10000000)
            mMediaRecorder.setVideoFrameRate(30)
            mMediaRecorder.setVideoSize(metrics.widthPixels, metrics.heightPixels)
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264)
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            try {
                mMediaRecorder.prepare()
            } catch (e: IOException) {
                e.printStackTrace()
            }
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
            projection?.stop()
            Log.e("TEST","stopRecording done.")
        }

        fun Context.toast(message:String){
            Toast.makeText(applicationContext,message, Toast.LENGTH_SHORT).show()
        }

        private fun createPath() {
            var pathFile = File(Const.SavePath)
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
            val format = SimpleDateFormat(solarDatePattern)
            return format.format(date)
        }

        fun currentTimeToLong(): Long {
            return System.currentTimeMillis()
        }

        fun currentTimeToMinute(): String {
            return convertLongToTime(System.currentTimeMillis())
        }

        fun convertDateToLong(date: String): Long {
            val df = SimpleDateFormat(solarDatePattern)
            return df.parse(date).time
        }

        fun convertDateToDate(date: String): Date {
            val df = SimpleDateFormat(solarDatePattern)
            return df.parse(date)
        }

        fun getShiftTime(date:Date, shift:Int):Date {
            val cal = Calendar.getInstance() // creates calendar
            cal.time = date // sets calendar time/date
            cal.add(Calendar.MINUTE, Const.RecordingShift.toInt()) // adds one hour
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
            var end = matchTimeToLong(it.GameEnd?:"2030-01-01T00:00:00Z")
            val current = currentTimeToLong()
            //Log.e("TEST", "result=" + (current in (start + 1) until end))
            return current in (start + 1) until end
        }

        fun matchTimeToLong(time: String):Long {
            if(time == null || time.equals("null")) {
                return 0
            }
            // "GameEnd":"2020-07-07T18:00:00Z",
            val time = time.replace("T","").replace("Z","")
            return convertDateToLong(time)
        }

        fun matchTimeToDate(time: String):Date {
            if(time == null || time.equals("null")) {
                return Date()
            }
            // "GameEnd":"2020-07-07T18:00:00Z",
            val time = time.replace("T","").replace("Z","")
            return convertDateToDate(time)
        }
    }
}