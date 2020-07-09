package com.arttseng.screenrecorder

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.icu.util.UniversalTimeScale.toLong
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import com.arttseng.screenrecorder.tools.GameData
import okhttp3.Call
import okhttp3.Callback
import okhttp3.Request
import okhttp3.Response
import org.json.JSONException
import org.json.JSONObject
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

        fun startRecord(ctx:Context, resultCode: Int, data: Intent, filename: String) {
            mMediaRecorder = MediaRecorder()
            manager = ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

            projection = manager?.getMediaProjection(resultCode, data)
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
            Log.e("TEST", "startRecord:" + currentTimeToMinute())
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

        fun stopRecording() {
            mMediaRecorder.setOnErrorListener(null)
            mMediaRecorder.setOnInfoListener(null)
            mMediaRecorder.setPreviewDisplay(null)
            try {
                mMediaRecorder.stop()
            } catch (e: IllegalStateException) {
                e.printStackTrace()
            } catch (e: RuntimeException) {
                e.printStackTrace()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            projection?.stop()
        }

        fun Context.toast(message:String){
            Toast.makeText(applicationContext,message, Toast.LENGTH_SHORT).show()
        }

        private fun createPath() {
            var pathFile = File(Consts.VideoPath)
            if(!pathFile.exists()) {
                val success = pathFile.mkdirs()
                if (success) Log.e("TEST","Directory path was created successfully")
                else         Log.e("TEST","Failed to create directory path")
            }
        }

        fun getFilename(title:String):String {
            createPath()
            return Consts.VideoPath + File.separator  + title +".mp4"
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

        fun minuteToLong(minute: Int):Long {
            return  minute*1000*60L
        }

        fun getDeviceName():String {
            return Build.MODEL // returns model name
        }

        fun getMatchTitle(match: GameData):String {
            return match.LeagueName + "_" + match.GameStart.replace("-","").replace(":","")
        }

        fun isAfterStartBeforeEnd(it: GameData):Boolean {
            Log.e("TEST", "it=" + it.id + "," + it.GameStart+ "," + it.GameEnd)
            val start = matchTimeToLong( it.GameStart)
            var end = it.GameEnd?.let { it1 -> matchTimeToLong(it1) }!!
            val current = currentTimeToLong()
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

        fun httpGet(url: String, callBack: SolarCallBack) {
            Log.e("TEST", "httpGet url=" + url)
            val request = Request.Builder()
                .url(url)
                .build()
            MyApplication.get().okHttpClient.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    callBack?.run { onErr(e.message) }
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    //responseHandle(response, callBack)
                    callBack?.run {
                        onOK(response.body().toString())
                    }
                }
            })
        }
    }
}