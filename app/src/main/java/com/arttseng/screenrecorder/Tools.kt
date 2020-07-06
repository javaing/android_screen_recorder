package com.arttseng.screenrecorder

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.widget.Toast
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
    }
}