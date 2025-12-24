package com.example.anasdemoapplication.model

import androidx.annotation.Keep
import androidx.compose.runtime.Stable

@Keep
@Stable
data class TotalHoldingsUiState(
    val holdings: List<HoldingUiState> = emptyList(),
    val totalInvestment: Double = 0.0,
    val currentValue: Double = 0.0,
    val totalPnL: Double = 0.0
) {
    @Keep
    @Stable
    data class HoldingUiState(
        val name: String,
        val quantity: Int,
        val profitAndLoss: Double,
        val lastTradedPrice: Double,
        val averagePrice: Double
    )
}

fun TotalHolding.toTotalHoldingsUiState(): TotalHoldingsUiState {
    val holdingsList = data.userHolding.map { it.toHoldingUiState() }
    return TotalHoldingsUiState(
        holdings = holdingsList,
        totalInvestment = holdingsList.sumOf { it.averagePrice * it.quantity },
        currentValue = holdingsList.sumOf { it.lastTradedPrice * it.quantity },
        totalPnL = holdingsList.sumOf { it.profitAndLoss }
    )
}

fun TotalHolding.Data.UserHolding.toHoldingUiState() = TotalHoldingsUiState.HoldingUiState(
    name = symbol,
    quantity = quantity,
    profitAndLoss = ((lastTradedPrice - averagePrice) * quantity),
    lastTradedPrice = lastTradedPrice,
    averagePrice = averagePrice
)