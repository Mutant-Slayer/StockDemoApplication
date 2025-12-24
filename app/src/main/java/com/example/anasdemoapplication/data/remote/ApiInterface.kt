package com.example.anasdemoapplication.data.remote

import retrofit2.Response
import retrofit2.http.GET

interface ApiInterface {
    @GET(".")
    suspend fun getTotalHoldingsData(): Response<TotalHolding>
}