package com.example.anasdemoapplication.domain

import com.example.anasdemoapplication.data.remote.RequestResult
import com.example.anasdemoapplication.data.remote.TotalHolding

interface StockRepository {
    suspend fun getHoldingList(): RequestResult<TotalHolding>
}