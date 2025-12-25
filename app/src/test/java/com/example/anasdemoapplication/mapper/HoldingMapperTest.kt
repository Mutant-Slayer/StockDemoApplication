package com.example.anasdemoapplication.mapper

import com.example.anasdemoapplication.data.local.db.UserHoldingEntity
import com.example.anasdemoapplication.data.mapper.toDomain
import com.example.anasdemoapplication.data.mapper.toEntities
import com.example.anasdemoapplication.data.mapper.toEntity
import com.example.anasdemoapplication.data.mapper.toHoldingUiState
import com.example.anasdemoapplication.data.mapper.toTotalHolding
import com.example.anasdemoapplication.data.mapper.toTotalHoldingsUiState
import com.example.anasdemoapplication.data.remote.TotalHolding
import com.example.anasdemoapplication.domain.ScreenUiState
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import org.junit.Test

class HoldingMapperTest {

    @Test
    fun `toTotalHoldingsUiState with empty holdings returns zero values`() {
        val totalHolding = TotalHolding(
            data = TotalHolding.Data(userHolding = emptyList())
        )

        // When
        val result = totalHolding.toTotalHoldingsUiState()

        // Then
        assertEquals(0.0, result.totalInvestment)
        assertEquals(0.0, result.currentValue)
        assertEquals(0.0, result.totalPnL)
        assertEquals(0.0, result.todaysPnL)
        assertEquals(0.0, result.pnlPercentage)
        assertEquals(ScreenUiState.Success, result.screenUiState)
        assertTrue(result.holdings.isEmpty())
    }

    @Test
    fun `toTotalHoldingsUiState with single holding calculates correctly`() {
        val holding = createUserHolding(
            symbol = "MAHABANK",
            quantity = 100,
            ltp = 50.0,
            avgPrice = 40.0,
            close = 45.0
        )
        val totalHolding = TotalHolding(
            data = TotalHolding.Data(userHolding = listOf(holding))
        )

        // When
        val result = totalHolding.toTotalHoldingsUiState()

        // Then
        assertEquals(4000.0, result.totalInvestment, 0.01)
        assertEquals(5000.0, result.currentValue, 0.01)
        assertEquals(1000.0, result.totalPnL, 0.01)
        assertEquals(-500.0, result.todaysPnL, 0.01)
        assertEquals(25.0, result.pnlPercentage, 0.01)
        assertEquals(1, result.holdings.size)
    }

    @Test
    fun `toTotalHoldingsUiState with multiple holdings calculates total correctly`() {
        // Given
        val holdings = listOf(
            createUserHolding("STOCK1", 100, 50.0, 40.0, 45.0),
            createUserHolding("STOCK2", 200, 30.0, 25.0, 28.0),
            createUserHolding("STOCK3", 150, 100.0, 90.0, 95.0)
        )
        val totalHolding = TotalHolding(
            data = TotalHolding.Data(userHolding = holdings)
        )

        // When
        val result = totalHolding.toTotalHoldingsUiState()

        // Then
        assertEquals(22500.0, result.totalInvestment, 0.01)
        assertEquals(26000.0, result.currentValue, 0.01)
        assertEquals(3500.0, result.totalPnL, 0.01)
        assertEquals(-1650.0, result.todaysPnL, 0.01)
        assertEquals(15.56, result.pnlPercentage, 0.01)
    }

    @Test
    fun `toTotalHoldingsUiState with negative P&L calculates correctly`() {
        // Given
        val holdings = listOf(
            createUserHolding("LOSS1", 100, 30.0, 50.0, 40.0),
            createUserHolding("LOSS2", 200, 20.0, 35.0, 25.0)
        )
        val totalHolding = TotalHolding(
            data = TotalHolding.Data(userHolding = holdings)
        )

        // When
        val result = totalHolding.toTotalHoldingsUiState()

        // Then
        assertEquals(12000.0, result.totalInvestment, 0.01)
        assertEquals(7000.0, result.currentValue, 0.01)
        assertEquals(-5000.0, result.totalPnL, 0.01)
        assertTrue("P&L should be negative", result.totalPnL < 0)
        assertEquals(-41.67, result.pnlPercentage, 0.01)
        assertTrue("P&L percentage should be negative", result.pnlPercentage < 0)
    }

    @Test
    fun `toTotalHoldingsUiState with mixed profit and loss holdings`() {
        // Given
        val holdings = listOf(
            createUserHolding("PROFIT", 100, 60.0, 40.0, 50.0),  // +2000
            createUserHolding("LOSS", 100, 30.0, 50.0, 40.0)     // -2000
        )
        val totalHolding = TotalHolding(
            data = TotalHolding.Data(userHolding = holdings)
        )

        // When
        val result = totalHolding.toTotalHoldingsUiState()

        // Then
        assertEquals(9000.0, result.totalInvestment, 0.01)
        assertEquals(9000.0, result.currentValue, 0.01)
        assertEquals(0.0, result.totalPnL, 0.01)
        assertEquals(0.0, result.pnlPercentage, 0.01)
    }

    @Test
    fun `toTotalHoldingsUiState calculates today's PnL correctly`() {
        // Given
        val holdings = listOf(
            createUserHolding("GAIN", 100, 50.0, 40.0, 48.0),
            createUserHolding("LOSS", 100, 30.0, 25.0, 35.0)
        )
        val totalHolding = TotalHolding(
            data = TotalHolding.Data(userHolding = holdings)
        )

        // When
        val result = totalHolding.toTotalHoldingsUiState()

        // Then
        assertEquals(300.0, result.todaysPnL, 0.01)
    }

    @Test
    fun `toTotalHoldingsUiState handles zero investment edge case`() {
        // Given
        val holdings = listOf(
            createUserHolding("FREE", 100, 50.0, 0.0, 45.0)
        )
        val totalHolding = TotalHolding(
            data = TotalHolding.Data(userHolding = holdings)
        )

        // When
        val result = totalHolding.toTotalHoldingsUiState()

        // Then
        assertEquals(0.0, result.totalInvestment)
        assertEquals(5000.0, result.currentValue, 0.01)
        assertEquals(5000.0, result.totalPnL, 0.01)
        assertEquals(0.0, result.pnlPercentage)
    }

    @Test
    fun `toTotalHoldingsUiState with decimal values maintains precision`() {
        // Given
        val holdings = listOf(
            createUserHolding("PRECISE", 150, 38.75, 35.50, 37.25)
        )
        val totalHolding = TotalHolding(
            data = TotalHolding.Data(userHolding = holdings)
        )

        // When
        val result = totalHolding.toTotalHoldingsUiState()

        // Then
        assertEquals(5325.00, result.totalInvestment, 0.01)
        assertEquals(5812.50, result.currentValue, 0.01)
        assertEquals(487.50, result.totalPnL, 0.01)
        assertEquals(9.15, result.pnlPercentage, 0.01)
    }

    @Test
    fun `toTotalHoldingsUiState with large quantities and prices`() {
        // Given
        val holdings = listOf(
            createUserHolding("LARGE", 10000, 2500.0, 2400.0, 2450.0)
        )
        val totalHolding = TotalHolding(
            data = TotalHolding.Data(userHolding = holdings)
        )

        // When
        val result = totalHolding.toTotalHoldingsUiState()

        // Then
        assertEquals(24000000.0, result.totalInvestment, 0.01)
        assertEquals(25000000.0, result.currentValue, 0.01)
        assertEquals(1000000.0, result.totalPnL, 0.01)
    }

    @Test
    fun `toTotalHoldingsUiState always returns Success screen state`() {
        // Given
        val totalHolding = TotalHolding(
            data = TotalHolding.Data(userHolding = emptyList())
        )

        // When
        val result = totalHolding.toTotalHoldingsUiState()

        // Then
        assertEquals(ScreenUiState.Success, result.screenUiState)
    }

    @Test
    fun `toHoldingUiState maps single holding correctly`() {
        // Given
        val holding = createUserHolding(
            symbol = "MAHABANK",
            quantity = 990,
            ltp = 38.05,
            avgPrice = 35.0,
            close = 40.0
        )

        // When
        val result = holding.toHoldingUiState()

        // Then
        assertEquals("MAHABANK", result.name)
        assertEquals(990, result.quantity)
        assertEquals(38.05, result.lastTradedPrice)
        assertEquals(35.0, result.averagePrice)
        assertEquals(40.0, result.close)
        assertEquals(3019.5, result.profitAndLoss, 0.01)
    }

    @Test
    fun `toHoldingUiState with negative profit calculates correctly`() {
        // Given
        val holding = createUserHolding(
            symbol = "LOSS_STOCK",
            quantity = 100,
            ltp = 30.0,
            avgPrice = 50.0,
            close = 40.0
        )

        // When
        val result = holding.toHoldingUiState()

        // Then
        assertEquals(-2000.0, result.profitAndLoss, 0.01)
        assertTrue("Profit should be negative", result.profitAndLoss < 0)
    }

    @Test
    fun `toHoldingUiState with zero quantity`() {
        // Given
        val holding = createUserHolding(
            symbol = "ZERO",
            quantity = 0,
            ltp = 50.0,
            avgPrice = 40.0,
            close = 45.0
        )

        // When
        val result = holding.toHoldingUiState()

        // Then
        assertEquals(0, result.quantity)
        assertEquals(0.0, result.profitAndLoss, 0.01)
    }

    @Test
    fun `toEntity converts UserHolding to UserHoldingEntity correctly`() {
        // Given
        val holding = createUserHolding(
            symbol = "ICICI",
            quantity = 100,
            ltp = 118.25,
            avgPrice = 110.0,
            close = 105.0
        )

        // When
        val entity = holding.toEntity()

        // Then
        assertEquals("ICICI", entity.symbol)
        assertEquals(100, entity.quantity)
        assertEquals(118.25, entity.lastTradedPrice)
        assertEquals(110.0, entity.averagePrice)
        assertEquals(105.0, entity.close)
    }

    @Test
    fun `toEntities converts list of holdings to entities`() {
        // Given
        val holdings = listOf(
            createUserHolding("STOCK1", 100, 50.0, 40.0, 45.0),
            createUserHolding("STOCK2", 200, 30.0, 25.0, 28.0),
            createUserHolding("STOCK3", 150, 100.0, 90.0, 95.0)
        )

        // When
        val entities = holdings.toEntities()

        // Then
        assertEquals(3, entities.size)
        assertEquals("STOCK1", entities[0].symbol)
        assertEquals("STOCK2", entities[1].symbol)
        assertEquals("STOCK3", entities[2].symbol)
    }

    @Test
    fun `toEntities with empty list returns empty list`() {
        // Given
        val holdings = emptyList<TotalHolding.Data.UserHolding>()

        // When
        val entities = holdings.toEntities()

        // Then
        assertTrue(entities.isEmpty())
    }

    @Test
    fun `toDomain converts UserHoldingEntity back to UserHolding`() {
        // Given
        val entity = UserHoldingEntity(
            symbol = "SBI",
            quantity = 150,
            lastTradedPrice = 550.05,
            averagePrice = 501.0,
            close = 590.0
        )

        // When
        val domain = entity.toDomain()

        // Then
        assertEquals("SBI", domain.symbol)
        assertEquals(150, domain.quantity)
        assertEquals(550.05, domain.lastTradedPrice)
        assertEquals(501.0, domain.averagePrice)
        assertEquals(590.0, domain.close)
    }

    @Test
    fun `toTotalHolding converts list of entities to TotalHolding`() {
        // Given
        val entities = listOf(
            UserHoldingEntity("STOCK1", 100.00, 50.0, 40.0, 45),
            UserHoldingEntity("STOCK2", 200.00, 30.0, 25.0, 28)
        )

        // When
        val totalHolding = entities.toTotalHolding()

        // Then
        assertEquals(2, totalHolding.data.userHolding.size)
        assertEquals("STOCK1", totalHolding.data.userHolding[0].symbol)
        assertEquals("STOCK2", totalHolding.data.userHolding[1].symbol)
    }

    @Test
    fun `toTotalHolding with empty list creates TotalHolding with empty data`() {
        // Given
        val entities = emptyList<UserHoldingEntity>()

        // When
        val totalHolding = entities.toTotalHolding()

        // Then
        assertTrue(totalHolding.data.userHolding.isEmpty())
    }

    @Test
    fun `domain to entity to domain mapping preserves data`() {
        // Given
        val original = createUserHolding(
            symbol = "RELIANCE",
            quantity = 50,
            ltp = 2500.0,
            avgPrice = 2450.0,
            close = 2600.0
        )

        // When
        val entity = original.toEntity()
        val backToDomain = entity.toDomain()

        // Then
        assertEquals(original.symbol, backToDomain.symbol)
        assertEquals(original.quantity, backToDomain.quantity)
        assertEquals(original.lastTradedPrice, backToDomain.lastTradedPrice)
        assertEquals(original.averagePrice, backToDomain.averagePrice)
        assertEquals(original.close, backToDomain.close)
    }

    @Test
    fun `entity to domain to entity mapping preserves data`() {
        // Given
        val original = UserHoldingEntity(
            symbol = "HDFC",
            quantity = 75,
            lastTradedPrice = 1800.25,
            averagePrice = 1750.0,
            close = 1700.0
        )

        // When
        val domain = original.toDomain()
        val backToEntity = domain.toEntity()

        // Then
        assertEquals(original.symbol, backToEntity.symbol)
        assertEquals(original.quantity, backToEntity.quantity)
        assertEquals(original.lastTradedPrice, backToEntity.lastTradedPrice)
        assertEquals(original.averagePrice, backToEntity.averagePrice)
        assertEquals(original.close, backToEntity.close)
    }

    @Test
    fun `list mapping through entities preserves all data`() {
        // Given
        val originalHoldings = listOf(
            createUserHolding("A", 100, 50.0, 40.0, 45.0),
            createUserHolding("B", 200, 30.0, 25.0, 28.0)
        )

        // When
        val entities = originalHoldings.toEntities()
        val totalHolding = entities.toTotalHolding()
        val resultHoldings = totalHolding.data.userHolding

        // Then
        assertEquals(originalHoldings.size, resultHoldings.size)
        originalHoldings.forEachIndexed { index, original ->
            val result = resultHoldings[index]
            assertEquals(original.symbol, result.symbol)
            assertEquals(original.quantity, result.quantity)
            assertEquals(original.lastTradedPrice, result.lastTradedPrice)
            assertEquals(original.averagePrice, result.averagePrice)
            assertEquals(original.close, result.close)
        }
    }

    private fun createUserHolding(
        symbol: String,
        quantity: Int,
        ltp: Double,
        avgPrice: Double,
        close: Double
    ): TotalHolding.Data.UserHolding {
        return TotalHolding.Data.UserHolding(
            symbol = symbol,
            quantity = quantity,
            lastTradedPrice = ltp,
            averagePrice = avgPrice,
            close = close
        )
    }
}