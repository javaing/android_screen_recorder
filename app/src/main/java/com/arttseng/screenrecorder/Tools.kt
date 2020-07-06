package com.arttseng.screenrecorder

import android.content.Context
import android.content.Intent
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Environment
import android.util.Log
import android.widget.Toast
import java.io.IOException

class Tools {
    companion object {
        lateinit var manager: MediaProjectionManager
        lateinit var filename :String
        lateinit var mMediaRecorder: MediaRecorder
        lateinit var projection: MediaProjection
        lateinit var virtualDisplay: VirtualDisplay



        fun startRecord(ctx:Context, resultCode: Int, data: Intent) {
            mMediaRecorder = MediaRecorder()
            filename = ctx.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.path +"/mobile01.mp4"
            manager = ctx.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

            Log.e("TEST", "1")
            projection = manager?.getMediaProjection(resultCode, data)
            Log.e("TEST", "2")
            setUpMediaRecorder(ctx)
            val metrics = ctx.getResources().getDisplayMetrics()
            virtualDisplay = projection.createVirtualDisplay("ScreenRecording",
                metrics.widthPixels, metrics.heightPixels, metrics.densityDpi, DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR,
                mMediaRecorder.getSurface(), null, null);

            mMediaRecorder.start()
        }

        private fun setUpMediaRecorder(ctx: Context) {
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
    }
}