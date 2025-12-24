package com.example.anasdemoapplication.data

import com.example.anasdemoapplication.model.RequestResult
import com.example.anasdemoapplication.model.TotalHolding
import com.example.anasdemoapplication.network.ApiInterface
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl
@Inject
constructor(
    private val apiInterface: ApiInterface
) : StockRepository {
    override fun getHoldingList(): RequestResult<TotalHolding> {
        return try {
            val response = apiInterface.getTotalHoldingsData()
            if (response.isSuccessful) {
                RequestResult.Success(response.body()!!)
            } else {
                RequestResult.Error(Exception(response.message()))
            }
        } catch (e: Exception) {
            RequestResult.Error(e)
        }
    }
}