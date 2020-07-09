package com.arttseng.screenrecorder.tools

import com.arttseng.screenrecorder.Consts
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface ApiClient {
    @GET(Consts.MatchAPI) fun getGameList(): Deferred<Response<List<GameData>>>
    @POST(Consts.UpdateStatusAPI) fun updateStatus(@Body device : String): Deferred<Response<Void>>
}