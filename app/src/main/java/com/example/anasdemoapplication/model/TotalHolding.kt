package com.example.anasdemoapplication.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class TotalHolding(
    @SerializedName("data")
    val data: Data
)