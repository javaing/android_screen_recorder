package com.arttseng.screenrecorder

class Const {
    companion object {
        val KEY_MEDIA_PROJECTION_RESULTCODE = "resultCode"
        val KEY_MEDIA_PROJECTION_INTENT = "dataIntent"
        val URL = "url"
        val Title = "title"

        var RecordingLength = 1000*60*1L
        var ScanPeriod = 1000*60*5L

        //const val BaseURL = "http://203.69.207.107:9001/smzb/api/"
        var BaseURL = "http://10.80.1.18:9001/smzb/api/"
        var SavePath = "/storage/emulated/0/Pictures/screenshots"
        var RecordingShift = "0"

        const val MatchAPI = "GetGameList"
        const val UpdateStatusAPI = "UpdateStatus"
        const val SMTV = "https://smtv.io"
    }
}