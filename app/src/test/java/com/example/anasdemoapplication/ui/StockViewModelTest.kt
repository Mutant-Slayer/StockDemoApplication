package com.example.anasdemoapplication.ui

import app.cash.turbine.test
import com.example.anasdemoapplication.data.remote.RequestResult
import com.example.anasdemoapplication.data.remote.TotalHolding
import com.example.anasdemoapplication.domain.ScreenUiState
import com.example.anasdemoapplication.domain.StockRepository
import com.example.anasdemoapplication.utils.ConnectivityReceiver
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class StockViewModelTest {

    private lateinit var stockRepository: StockRepository
    private lateinit var connectivityReceiver: ConnectivityReceiver
    private lateinit var viewModel: StockViewModel

    private val testDispatcher = StandardTestDispatcher()

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        stockRepository = mockk()
        connectivityReceiver = mockk {
            every { isConnected } returns MutableStateFlow(true)
        }
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state has default values with loading state`() = runTest {
        // Given
        coEvery { stockRepository.getHoldingList() } coAnswers {
            // Delay to keep it in loading state
            kotlinx.coroutines.delay(1000)
            RequestResult.Success(createMockResponse(emptyList()))
        }

        // When
        viewModel = StockViewModel(stockRepository, connectivityReceiver)

        // Then - check state before completion
        viewModel.totalHoldings.test {
            val initialState = awaitItem()
            assertEquals(ScreenUiState.Loading, initialState.screenUiState)
            assertEquals(0.0, initialState.currentValue)
            assertEquals(0.0, initialState.totalInvestment)
            assertEquals(0.0, initialState.totalPnL)
            assertEquals(0.0, initialState.todaysPnL)
        }
    }

    @Test
    fun `getAllHoldings with empty list returns success with zero values`() = runTest {
        val emptyResponse = createMockResponse(emptyList())
        coEvery { stockRepository.getHoldingList() } returns RequestResult.Success(emptyResponse)

        // When
        viewModel = StockViewModel(stockRepository, connectivityReceiver)
        advanceUntilIdle()

        // Then
        viewModel.totalHoldings.test {
            val state = awaitItem()
            assertEquals(ScreenUiState.Success, state.screenUiState)
            assertEquals(0.0, state.currentValue)
            assertEquals(0.0, state.totalInvestment)
            assertEquals(0.0, state.totalPnL)
        }
    }

    @Test
    fun `when stock data loads successfully, state updates correctly`() = runTest {
        val mockHolding = TotalHolding.Data.UserHolding(
            symbol = "MAHABANK",
            quantity = 990,
            lastTradedPrice = 38.05,
            averagePrice = 35.0,
            close = 40.0
        )

        val mockResponse = TotalHolding(
            data = TotalHolding.Data(userHolding = listOf(mockHolding))
        )

        coEvery { stockRepository.getHoldingList() } returns RequestResult.Success(mockResponse)

        // When
        viewModel = StockViewModel(stockRepository, connectivityReceiver)
        advanceUntilIdle()

        // Then
        viewModel.totalHoldings.test {
            val state = awaitItem()
            assertEquals(ScreenUiState.Success, state.screenUiState)
            assertTrue(state.currentValue > 0.0)
        }
    }

    @Test
    fun `getAllHoldings with RequestResult Error updates state to error`() = runTest {
        coEvery { stockRepository.getHoldingList() } returns RequestResult.Error(Exception("Network error"))

        // When
        viewModel = StockViewModel(stockRepository, connectivityReceiver)
        advanceUntilIdle()

        // Then
        viewModel.totalHoldings.test {
            val state = awaitItem()
            assertEquals(ScreenUiState.Error, state.screenUiState)
        }
    }

    @Test
    fun `init block calls getAllHoldings exactly once`() = runTest {
        val response = createMockResponse(emptyList())
        coEvery { stockRepository.getHoldingList() } returns RequestResult.Success(response)

        // When
        viewModel = StockViewModel(stockRepository, connectivityReceiver)
        advanceUntilIdle()

        // Then
        coVerify(exactly = 1) { stockRepository.getHoldingList() }
    }

    @Test
    fun `getAllHoldings with different error messages all result in error state`() = runTest {
        val errorMessages = listOf(
            "Network timeout",
            "401 Unauthorized",
            "500 Internal Server Error",
            "Connection refused"
        )

        errorMessages.forEach { errorMsg ->
            coEvery { stockRepository.getHoldingList() } returns RequestResult.Error(
                Exception(
                    errorMsg
                )
            )

            // When
            viewModel = StockViewModel(stockRepository, connectivityReceiver)
            advanceUntilIdle()

            // Then
            viewModel.totalHoldings.test {
                val state = awaitItem()
                assertEquals(ScreenUiState.Error, state.screenUiState)
            }
        }
    }

    @Test
    fun `getAllHoldings handles exception gracefully`() = runTest {
        // Given
        coEvery { stockRepository.getHoldingList() } throws Exception("Unexpected error")

        // When
        viewModel = StockViewModel(stockRepository, connectivityReceiver)
        advanceUntilIdle()

        // Then
        viewModel.totalHoldings.test {
            val state = awaitItem()
            assertEquals(ScreenUiState.Error, state.screenUiState)
        }
    }

    // Helper to create test data easily
    private fun createMockResponse(holdings: List<TotalHolding.Data.UserHolding>): TotalHolding {
        return TotalHolding(
            data = TotalHolding.Data(userHolding = holdings)
        )
    }
}