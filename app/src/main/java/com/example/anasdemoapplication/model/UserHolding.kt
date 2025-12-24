package com.example.anasdemoapplication.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class UserHolding(
    @SerializedName("avgPrice")
    val averagePrice: Double,
    @SerializedName("close")
    val close: Double,
    @SerializedName("ltp")
    val lastTradedPrice: Double,
    @SerializedName("quantity")
    val quantity: Int,
    @SerializedName("symbol")
    val symbol: String
)