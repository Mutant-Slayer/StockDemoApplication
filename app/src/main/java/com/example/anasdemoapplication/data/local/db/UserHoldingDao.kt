package com.example.anasdemoapplication.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface UserHoldingDao {
    @Query("SELECT * FROM user_holdings")
    suspend fun getAllHoldings(): List<UserHoldingEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllHoldings(holdings: List<UserHoldingEntity>)

    @Query("DELETE FROM user_holdings")
    suspend fun deleteAllHoldings()
}