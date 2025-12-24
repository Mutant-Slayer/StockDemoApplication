package com.example.anasdemoapplication.data

import com.example.anasdemoapplication.db.UserHoldingDao
import com.example.anasdemoapplication.model.RequestResult
import com.example.anasdemoapplication.model.TotalHolding
import com.example.anasdemoapplication.model.toEntities
import com.example.anasdemoapplication.model.toTotalHolding
import com.example.anasdemoapplication.network.ApiInterface
import com.example.anasdemoapplication.utils.ConnectivityReceiver
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StockRepositoryImpl
@Inject
constructor(
    private val apiInterface: ApiInterface,
    private val userHoldingDao: UserHoldingDao,
    private val connectivityReceiver: ConnectivityReceiver,
) : StockRepository {

    override suspend fun getHoldingList(): RequestResult<TotalHolding> {
        return try {
            if (connectivityReceiver.isCurrentlyConnected()) {
                val response = apiInterface.getTotalHoldingsData()
                if (response.isSuccessful) {
                    response.body()?.let { totalHolding ->
                        val entities = totalHolding.data.userHolding.toEntities()
                        userHoldingDao.deleteAllHoldings()
                        userHoldingDao.insertAllHoldings(entities)
                        RequestResult.Success(totalHolding)
                    } ?: RequestResult.Error(Exception("Empty data"))
                } else {
                    RequestResult.Error(Exception(response.message()))
                }
            } else {
                val cachedHoldings = userHoldingDao.getAllHoldings()
                if (cachedHoldings.isNotEmpty()) {
                    val totalHolding = cachedHoldings.toTotalHolding()
                    RequestResult.Success(totalHolding)
                } else {
                    RequestResult.Error(Exception("No internet and no stored data available"))
                }
            }
        } catch (e: Exception) {
            val cachedHoldings = userHoldingDao.getAllHoldings()
            if (cachedHoldings.isNotEmpty()) {
                val totalHolding = cachedHoldings.toTotalHolding()
                RequestResult.Success(totalHolding)
            } else {
                RequestResult.Error(e)
            }
        }
    }
}