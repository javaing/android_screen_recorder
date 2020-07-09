package com.arttseng.screenrecorder.tools


import com.arttseng.screenrecorder.Consts
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class GameData (
    val id : Int,
    val AnchorID : Int,
    val AnchorName : String,
    val LeagueName : String,
    val GameStart : String,
    var GameEnd : String?=null,
    var Url : String=Consts.BaseURL,
    var MobileNumber : String?=null,
    val Status : Int
)