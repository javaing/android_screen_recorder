package com.arttseng.screenrecorder.tools

import com.arttseng.screenrecorder.Const
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface ApiClient {
    @GET(Const.MatchAPI) fun getGameList(): Deferred<Response<List<GameData>>>

    //UpdateStatus?id=3&MobileNumber=4341231
    //@FormUrlEncoded
    @PUT(Const.UpdateStatusAPI)
    fun updateStatus(
        @Query("id") id: Int,
        @Query("MobileNumber") MobileNumber: String,
        @Query("device") device: String,
        @Query("version") version: String
    ): Deferred<Response<Void>>


}