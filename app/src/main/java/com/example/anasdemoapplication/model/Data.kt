package com.example.anasdemoapplication.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class Data(
    @SerializedName("userHolding")
    val userHolding: List<UserHolding>
)