package com.example.anasdemoapplication.network

import com.example.anasdemoapplication.model.TotalHolding
import retrofit2.Response
import retrofit2.http.GET

interface ApiInterface {
    @GET(".")
    suspend fun getTotalHoldingsData(): Response<TotalHolding>
}