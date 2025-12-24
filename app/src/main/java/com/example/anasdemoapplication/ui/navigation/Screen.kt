package com.example.anasdemoapplication.ui.navigation

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable
data object Fund : NavKey

@Serializable
data object Invest : NavKey

@Serializable
data object Order : NavKey

@Serializable
data object Portfolio : NavKey

@Serializable
data object WatchList : NavKey