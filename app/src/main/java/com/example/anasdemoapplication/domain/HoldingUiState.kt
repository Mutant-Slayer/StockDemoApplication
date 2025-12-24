package com.example.anasdemoapplication.domain

import androidx.annotation.Keep
import androidx.compose.runtime.Stable

@Keep
@Stable
data class TotalHoldingsUiState(
    val holdings: List<HoldingUiState> = emptyList(),
    val totalInvestment: Double = 0.0,
    val currentValue: Double = 0.0,
    val totalPnL: Double = 0.0,
    val todaysPnL: Double = 0.0,
    val pnlPercentage: Double = 0.0,
    val screenUiState: ScreenUiState = ScreenUiState.Loading
) {
    @Keep
    @Stable
    data class HoldingUiState(
        val name: String,
        val quantity: Int,
        val profitAndLoss: Double,
        val lastTradedPrice: Double,
        val averagePrice: Double,
        val close: Double
    )
}