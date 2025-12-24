package com.example.anasdemoapplication.data.mapper

import com.example.anasdemoapplication.data.local.db.UserHoldingEntity
import com.example.anasdemoapplication.data.remote.TotalHolding
import com.example.anasdemoapplication.domain.TotalHoldingsUiState

fun TotalHolding.toTotalHoldingsUiState(): TotalHoldingsUiState {
    val holdingsList = data.userHolding.map { it.toHoldingUiState() }
    val totalInvestment = holdingsList.sumOf { it.averagePrice * it.quantity }
    val currentValue = holdingsList.sumOf { it.lastTradedPrice * it.quantity }
    val totalPnL = currentValue - totalInvestment
    val todaysPnL = holdingsList.sumOf { (it.close - it.lastTradedPrice) * it.quantity }
    val pnlPercentage = if (totalInvestment > 0) {
        (totalPnL / totalInvestment) * 100
    } else {
        0.0
    }

    return TotalHoldingsUiState(
        holdings = holdingsList,
        totalInvestment = totalInvestment,
        currentValue = currentValue,
        totalPnL = currentValue - totalInvestment,
        todaysPnL = todaysPnL,
        pnlPercentage = pnlPercentage
    )
}

fun TotalHolding.Data.UserHolding.toHoldingUiState() = TotalHoldingsUiState.HoldingUiState(
    name = symbol,
    quantity = quantity,
    profitAndLoss = ((lastTradedPrice - averagePrice) * quantity),
    lastTradedPrice = lastTradedPrice,
    averagePrice = averagePrice,
    close = close
)

fun TotalHolding.Data.UserHolding.toEntity(): UserHoldingEntity {
    return UserHoldingEntity(
        symbol = symbol,
        averagePrice = averagePrice,
        close = close,
        lastTradedPrice = lastTradedPrice,
        quantity = quantity
    )
}

fun List<TotalHolding.Data.UserHolding>.toEntities(): List<UserHoldingEntity> {
    return map { it.toEntity() }
}

fun UserHoldingEntity.toDomain(): TotalHolding.Data.UserHolding {
    return TotalHolding.Data.UserHolding(
        symbol = symbol,
        averagePrice = averagePrice,
        close = close,
        lastTradedPrice = lastTradedPrice,
        quantity = quantity
    )
}

fun List<UserHoldingEntity>.toTotalHolding(): TotalHolding {
    return TotalHolding(
        data = TotalHolding.Data(
            userHolding = this.map { it.toDomain() }
        )
    )
}