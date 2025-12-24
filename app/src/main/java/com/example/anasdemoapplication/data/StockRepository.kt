package com.example.anasdemoapplication.data

import com.example.anasdemoapplication.model.RequestResult
import com.example.anasdemoapplication.model.TotalHolding

interface StockRepository {
    fun getHoldingList(): RequestResult<TotalHolding>
}