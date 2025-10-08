package com.alihiz.filterflightscase.domain.usecase

import com.alihiz.filterflightscase.core.model.ApiResult
import com.alihiz.filterflightscase.domain.model.Flight
import com.alihiz.filterflightscase.domain.model.MapBounds
import com.alihiz.filterflightscase.domain.repository.FlightRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GetFlightsUseCaseTest {

    private class FakeRepoSuccess : FlightRepository {
        override suspend fun getFlightsInBounds(bounds: MapBounds): ApiResult<List<Flight>> =
            ApiResult.Success(
                listOf(
                    Flight(
                        icao24 = "ABC123",
                        callsign = null,
                        originCountry = "TR",
                        timePosition = null,
                        lastContact = null,
                        longitude = 28.9,
                        latitude = 41.0,
                        baroAltitude = null,
                        onGround = false,
                        velocity = null,
                        trueTrack = null,
                        verticalRate = null,
                        sensors = null,
                        geoAltitude = null,
                        squawk = null,
                        spi = false,
                        positionSource = 0
                    )
                )
            )

        override fun clearCache() { /* no-op */ }
    }

    private class FakeRepoError : FlightRepository {
        override suspend fun getFlightsInBounds(bounds: MapBounds): ApiResult<List<Flight>> =
            ApiResult.Exception(IllegalStateException("network"))

        override fun clearCache() { /* no-op */ }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `emits Success from repository`() = runTest {
        val useCase = GetFlightsUseCase(FakeRepoSuccess())
        val result = useCase(sampleBounds()).first()
        assertTrue(result is ApiResult.Success)
        val data = (result as ApiResult.Success).data
        assertEquals(1, data.size)
        assertEquals("ABC123", data.first().icao24)
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `emits Error when repository fails`() = runTest {
        val useCase = GetFlightsUseCase(FakeRepoError())
        val result = useCase(sampleBounds()).first()
        assertTrue(result is ApiResult.Exception || result is ApiResult.Error)
    }

    private fun sampleBounds() = MapBounds(
        minLat = 40.0,
        maxLat = 41.0,
        minLon = 28.0,
        maxLon = 29.0
    )
}


