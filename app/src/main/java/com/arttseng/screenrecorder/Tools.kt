package com.arttseng.screenrecorder

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.util.Log
import android.widget.Toast
import io.reactivex.Observable
import io.reactivex.Observer
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
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
        val solarDatePattern = "yyyy/MM/dd HH:mm"

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



        private fun ApiSubscribe(observable: Observable<*>,observer: Observer<Any>) {
            observable.subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(observer)
        }

        //    public static void httpDelete(String key,String url, final SolarCallBack callback) {
        //        Request request;
        //        try {
        //            request = deleteRequest(url+key);
        //        }
        //        catch (Exception e) {
        //            return;
        //        }
        //
        //        MyApp.get().getOkHttpClient().newCall(request).enqueue(new Callback() {
        //            @Override
        //            public void onFailure(Call call, IOException e) {
        //                e.printStackTrace();
        //            }
        //
        //            @Override
        //            public void onResponse(Call call, okhttp3.Response response) throws IOException {
        //                responseHandle(response, callback);
        //            }
        //        });
        //    }
        @Throws(IOException::class)
        private fun responseHandle(
            response: Response,
            callback: SolarCallBack
        ) {
            val str = response.body!!.string()
            if (response.code == 200) {
                try {
                    val jobj = JSONObject(str)
                    if (jobj["code"] is Int && jobj["code"] as Int == 0) {
                        callback.onOK(jobj)
                        return
                    }
                    if (jobj["code"] is Int && jobj["code"] as Int == 200) {
                        callback.onOK(jobj)
                        return
                    }
                    //val err: String = getMessageStr(jobj)
                    //callback.onErr(err)
                } catch (e: java.lang.Exception) {
                    e.printStackTrace()
                    callback.onErr("http error:")
                }
            } else if (response.code > 400) {
                //callback.onErr(MyApp.get().getString(R.string.please_relogin))
            } else {
                callback.onErr(str)
            }
        }

        fun getMessageStr(jobj: JSONObject): String? {
            var msg = ""
            try {
                if (jobj.has("message")) {
                    msg = jobj["message"] as String
                } else if (jobj.has("msg")) {
                    msg = jobj["msg"] as String
                } else if (jobj.has("result")) {
                    //{ "code": -1,"result": {"target": "http://alpha.solartech.gq","error": "验证码错误","code": -1,"phone": "886928867079"}}
                    if (jobj["result"] is JSONObject) {
                        val jobj2 = jobj["result"] as JSONObject
                        msg = jobj2["error"] as String
                    }
                } else {
                    msg = "" + jobj["message"]
                }
            } catch (e: JSONException) {
                e.printStackTrace()
            }
            return msg
        }

        fun httpGet(url: String, callBack: SolarCallBack) {
            val request = Request.Builder()
                .url(url)
                .build()
            MyApplication.get().getOkHttpClient().newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                }

                @Throws(IOException::class)
                override fun onResponse(call: Call, response: Response) {
                    responseHandle(response, callBack)
                }
            })
        }
    }
}