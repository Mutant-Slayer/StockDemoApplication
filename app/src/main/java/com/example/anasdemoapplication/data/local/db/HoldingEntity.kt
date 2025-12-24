package com.example.anasdemoapplication.data.local.db

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_holdings")
@Keep
data class UserHoldingEntity(
    @PrimaryKey
    @ColumnInfo(name = "symbol")
    val symbol: String,

    @ColumnInfo(name = "average_price")
    val averagePrice: Double,

    @ColumnInfo(name = "close")
    val close: Double,

    @ColumnInfo(name = "last_traded_price")
    val lastTradedPrice: Double,

    @ColumnInfo(name = "quantity")
    val quantity: Int
)
