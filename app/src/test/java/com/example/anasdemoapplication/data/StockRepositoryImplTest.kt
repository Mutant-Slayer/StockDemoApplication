package com.example.anasdemoapplication.data

import com.example.anasdemoapplication.data.local.db.UserHoldingDao
import com.example.anasdemoapplication.data.local.db.UserHoldingEntity
import com.example.anasdemoapplication.data.remote.ApiInterface
import com.example.anasdemoapplication.data.remote.RequestResult
import com.example.anasdemoapplication.data.remote.TotalHolding
import com.example.anasdemoapplication.utils.ConnectivityReceiver
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Before
import org.junit.Test
import retrofit2.Response
import java.io.IOException

class StockRepositoryImplTest {
    private lateinit var apiInterface: ApiInterface
    private lateinit var userHoldingDao: UserHoldingDao
    private lateinit var connectivityReceiver: ConnectivityReceiver
    private lateinit var repository: StockRepositoryImpl

    @Before
    fun setup() {
        apiInterface = mockk()
        userHoldingDao = mockk(relaxed = true)
        connectivityReceiver = mockk()
        repository = StockRepositoryImpl(apiInterface, userHoldingDao, connectivityReceiver)
    }

    @Test
    fun `getHoldingList when online and API success returns Success`() = runTest {
        // Given
        val mockHoldings = createMockHoldings()
        val mockResponse = createMockTotalHolding(mockHoldings)

        every { connectivityReceiver.isCurrentlyConnected() } returns true
        coEvery { apiInterface.getTotalHoldingsData() } returns Response.success(mockResponse)
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        val result = repository.getHoldingList()

        // Then
        assertTrue(result is RequestResult.Success)
        assertEquals(mockResponse, (result as RequestResult.Success).data)
    }

    @Test
    fun `getHoldingList when online and API success saves data to database`() = runTest {
        // Given
        val mockHoldings = createMockHoldings()
        val mockResponse = createMockTotalHolding(mockHoldings)

        every { connectivityReceiver.isCurrentlyConnected() } returns true
        coEvery { apiInterface.getTotalHoldingsData() } returns Response.success(mockResponse)
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        repository.getHoldingList()

        // Then
        coVerify { userHoldingDao.deleteAllHoldings() }
        coVerify { userHoldingDao.insertAllHoldings(any()) }
    }

    @Test
    fun `getHoldingList deletes old data before inserting new data`() = runTest {
        // Given
        val mockResponse = createMockTotalHolding(createMockHoldings())

        every { connectivityReceiver.isCurrentlyConnected() } returns true
        coEvery { apiInterface.getTotalHoldingsData() } returns Response.success(mockResponse)
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        repository.getHoldingList()

        // Then
        coVerify(ordering = io.mockk.Ordering.ORDERED) {
            userHoldingDao.deleteAllHoldings()
            userHoldingDao.insertAllHoldings(any())
        }
    }

    @Test
    fun `getHoldingList with multiple holdings saves all to database`() = runTest {
        // Given
        val holdings = listOf(
            createUserHolding("STOCK1", 100, 50.0, 40.0, 45.0),
            createUserHolding("STOCK2", 200, 30.0, 25.0, 28.0),
            createUserHolding("STOCK3", 150, 100.0, 90.0, 95.0)
        )
        val mockResponse = createMockTotalHolding(holdings)

        every { connectivityReceiver.isCurrentlyConnected() } returns true
        coEvery { apiInterface.getTotalHoldingsData() } returns Response.success(mockResponse)
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        repository.getHoldingList()

        // Then
        coVerify {
            userHoldingDao.insertAllHoldings(match { entities ->
                entities.size == 3 &&
                        entities[0].symbol == "STOCK1" &&
                        entities[1].symbol == "STOCK2" &&
                        entities[2].symbol == "STOCK3"
            })
        }
    }

    @Test
    fun `getHoldingList when online but API returns null body returns Error`() = runTest {
        // Given
        every { connectivityReceiver.isCurrentlyConnected() } returns true
        coEvery { apiInterface.getTotalHoldingsData() } returns Response.success(null)
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        val result = repository.getHoldingList()

        // Then
        assertTrue(result is RequestResult.Error)
        assertEquals("Empty data", (result as RequestResult.Error).exception.message)
    }

    @Test
    fun `getHoldingList when online but API returns error response returns Error`() = runTest {
        // Given
        every { connectivityReceiver.isCurrentlyConnected() } returns true
        coEvery { apiInterface.getTotalHoldingsData() } returns Response.error(
            404,
            "Not Found".toResponseBody()
        )
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        val result = repository.getHoldingList()

        // Then
        assertTrue(result is RequestResult.Error)
    }

    @Test
    fun `getHoldingList when offline but has cached data returns Success with cached data`() =
        runTest {
            // Given
            val cachedEntities = listOf(
                UserHoldingEntity("STOCK1", 100.00, 50.0, 40.0, 45),
                UserHoldingEntity("STOCK2", 200.00, 30.0, 25.0, 28)
            )

            every { connectivityReceiver.isCurrentlyConnected() } returns false
            coEvery { userHoldingDao.getAllHoldings() } returns cachedEntities

            // When
            val result = repository.getHoldingList()

            // Then
            assertTrue(result is RequestResult.Success)
            val data = (result as RequestResult.Success).data
            assertEquals(2, data.data.userHolding.size)
            assertEquals("STOCK1", data.data.userHolding[0].symbol)
            assertEquals("STOCK2", data.data.userHolding[1].symbol)
        }

    @Test
    fun `getHoldingList when offline with cached data does not call API`() = runTest {
        // Given
        val cachedEntities = listOf(
            UserHoldingEntity("CACHED", 100.00, 50.0, 40.0, 45)
        )

        every { connectivityReceiver.isCurrentlyConnected() } returns false
        coEvery { userHoldingDao.getAllHoldings() } returns cachedEntities

        // When
        repository.getHoldingList()

        // Then
        coVerify(exactly = 0) { apiInterface.getTotalHoldingsData() }
    }

    @Test
    fun `getHoldingList when offline with empty cache returns Error`() = runTest {
        // Given
        every { connectivityReceiver.isCurrentlyConnected() } returns false
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        val result = repository.getHoldingList()

        // Then
        assertTrue(result is RequestResult.Error)
        assertEquals(
            "No internet and no stored data available",
            (result as RequestResult.Error).exception.message
        )
    }

    @Test
    fun `getHoldingList when offline preserves all cached data fields`() = runTest {
        // Given
        val cachedEntity = UserHoldingEntity(
            symbol = "DETAILED",
            quantity = 150,
            lastTradedPrice = 38.75,
            averagePrice = 35.50,
            close = 37.25
        )

        every { connectivityReceiver.isCurrentlyConnected() } returns false
        coEvery { userHoldingDao.getAllHoldings() } returns listOf(cachedEntity)

        // When
        val result = repository.getHoldingList()

        // Then
        assertTrue(result is RequestResult.Success)
        val holding = (result as RequestResult.Success).data.data.userHolding[0]
        assertEquals("DETAILED", holding.symbol)
        assertEquals(150, holding.quantity)
        assertEquals(38.75, holding.lastTradedPrice)
        assertEquals(35.50, holding.averagePrice)
        assertEquals(37.25, holding.close)
    }

    @Test
    fun `getHoldingList when connectivity changes from online to offline mid-request`() = runTest {
        // Given
        var callCount = 0
        every { connectivityReceiver.isCurrentlyConnected() } answers {
            callCount++
            callCount == 1
        }

        val mockResponse = createMockTotalHolding(createMockHoldings())
        coEvery { apiInterface.getTotalHoldingsData() } returns Response.success(mockResponse)
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        val result = repository.getHoldingList()

        // Then
        assertTrue(result is RequestResult.Success)
    }

    @Test
    fun `getHoldingList does not save to database when API returns error`() = runTest {
        // Given
        every { connectivityReceiver.isCurrentlyConnected() } returns true
        coEvery { apiInterface.getTotalHoldingsData() } returns Response.error(
            404,
            "Not Found".toResponseBody()
        )
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        repository.getHoldingList()

        // Then
        coVerify(exactly = 0) { userHoldingDao.deleteAllHoldings() }
        coVerify(exactly = 0) { userHoldingDao.insertAllHoldings(any()) }
    }

    @Test
    fun `getHoldingList does not save to database when API returns null body`() = runTest {
        // Given
        every { connectivityReceiver.isCurrentlyConnected() } returns true
        coEvery { apiInterface.getTotalHoldingsData() } returns Response.success(null)
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        repository.getHoldingList()

        // Then
        coVerify(exactly = 0) { userHoldingDao.deleteAllHoldings() }
        coVerify(exactly = 0) { userHoldingDao.insertAllHoldings(any()) }
    }

    @Test
    fun `getHoldingList with empty API response saves empty list to database`() = runTest {
        // Given
        val emptyResponse = TotalHolding(
            data = TotalHolding.Data(userHolding = emptyList())
        )

        every { connectivityReceiver.isCurrentlyConnected() } returns true
        coEvery { apiInterface.getTotalHoldingsData() } returns Response.success(emptyResponse)
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        val result = repository.getHoldingList()

        // Then
        assertTrue(result is RequestResult.Success)
        coVerify { userHoldingDao.insertAllHoldings(match { it.isEmpty() }) }
    }

    @Test
    fun `getHoldingList preserves data integrity through mapping`() = runTest {
        // Given
        val originalHolding = createUserHolding(
            symbol = "INTEGRITY_TEST",
            quantity = 999,
            ltp = 123.45,
            avgPrice = 111.11,
            close = 130.00
        )
        val mockResponse = createMockTotalHolding(listOf(originalHolding))

        every { connectivityReceiver.isCurrentlyConnected() } returns true
        coEvery { apiInterface.getTotalHoldingsData() } returns Response.success(mockResponse)
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        val result = repository.getHoldingList()

        // Then
        assertTrue(result is RequestResult.Success)
        val returnedHolding = (result as RequestResult.Success).data.data.userHolding[0]
        assertEquals(originalHolding.symbol, returnedHolding.symbol)
        assertEquals(originalHolding.quantity, returnedHolding.quantity)
        assertEquals(originalHolding.lastTradedPrice, returnedHolding.lastTradedPrice)
        assertEquals(originalHolding.averagePrice, returnedHolding.averagePrice)
        assertEquals(originalHolding.close, returnedHolding.close)
    }

    @Test
    fun `getHoldingList when API throws Exception without cache returns Error`() = runTest {
        // Given
        val exception = IOException("Network error")

        every { connectivityReceiver.isCurrentlyConnected() } returns true
        coEvery { apiInterface.getTotalHoldingsData() } throws exception
        coEvery { userHoldingDao.getAllHoldings() } returns emptyList()

        // When
        val result = repository.getHoldingList()

        // Then
        assertTrue(result is RequestResult.Error)
        assertEquals(exception, (result as RequestResult.Error).exception)
    }

    private fun createMockHoldings(): List<TotalHolding.Data.UserHolding> {
        return listOf(
            createUserHolding("MAHABANK", 990, 38.05, 35.0, 40.0),
            createUserHolding("ICICI", 100, 118.25, 110.0, 105.0)
        )
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

    private fun createMockTotalHolding(
        holdings: List<TotalHolding.Data.UserHolding>
    ): TotalHolding {
        return TotalHolding(
            data = TotalHolding.Data(userHolding = holdings)
        )
    }
}