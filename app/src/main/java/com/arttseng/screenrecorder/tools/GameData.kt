package com.arttseng.screenrecorder.tools


import com.arttseng.screenrecorder.Const
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameData (
    val id : Int,
    val AnchorID : Int,
    val AnchorName : String,
    val LeagueName : String,
    val GameStart : String,
    var GameEnd : String?=null,
    var Url : String?,
    var MobileNumber : String?=null,
    val Status : Int
) {
    val zbUrl: String
        get() = if (Url.isNullOrEmpty()) {
            Const.BaseURL
        } else {
            if(Url!!.indexOf("http")<0)
                Const.SMZB + Url!!
            else
                Url!!
        }

}
