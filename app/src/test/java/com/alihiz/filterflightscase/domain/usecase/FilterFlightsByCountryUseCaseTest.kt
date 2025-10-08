package com.alihiz.filterflightscase.domain.usecase

import com.alihiz.filterflightscase.domain.model.Flight
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FilterFlightsByCountryUseCaseTest {

    private lateinit var useCase: FilterFlightsByCountryUseCase

    @Before
    fun setUp() {
        useCase = FilterFlightsByCountryUseCase()
    }

    @Test
    fun `returns all flights when country is null`() {
        val flights = sampleFlights()
        val result = useCase(flights, null)
        assertEquals(flights.size, result.size)
    }

    @Test
    fun `filters flights by origin country case-insensitively`() {
        val flights = sampleFlights()
        val result = useCase(flights, "turkey")
        assertEquals(2, result.size)
        assertEquals(setOf("TR1", "TR2"), result.map { it.callsign }.toSet())
    }

    private fun sampleFlights(): List<Flight> = listOf(
        flight("ABC123", "TR1", "Turkey"),
        flight("DEF456", "DE1", "Germany"),
        flight("GHI789", "TR2", "Turkey")
    )

    private fun flight(icao: String, callsign: String, country: String): Flight =
        Flight(
            icao24 = icao,
            callsign = callsign,
            originCountry = country,
            timePosition = null,
            lastContact = null,
            longitude = 28.0,
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
}


