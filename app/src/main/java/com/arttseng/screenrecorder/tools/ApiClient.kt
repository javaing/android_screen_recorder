package com.arttseng.screenrecorder.tools

import com.arttseng.screenrecorder.Consts
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.http.*

interface ApiClient {
    @GET(Consts.MatchAPI) fun getPartsAsync(): Deferred<Response<List<GameData>>>
    @POST(Consts.MatchAPI) fun addPartAsync(@Body newGame : GameData): Deferred<Response<Void>>
}