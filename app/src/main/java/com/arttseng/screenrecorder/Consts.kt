package com.arttseng.screenrecorder

class Consts {
    companion object {
        val KEY_MEDIA_PROJECTION_RESULTCODE = "resultCode"
        val KEY_MEDIA_PROJECTION_INTENT = "dataIntent"
        val URL = "url"
        val Title = "title"

        val VideoPath = "/storage/emulated/0/Pictures/screenshots"
        var RecordingLength = 1000*60*1L
        var ScanPeriod = 1000*60*5L

        const val BaseURL = "http://203.69.207.107:9001/smzb/api/"
        const val MatchAPI = "GetGameList"
        const val UpdateStatusAPI = "GetGameList"
    }
}