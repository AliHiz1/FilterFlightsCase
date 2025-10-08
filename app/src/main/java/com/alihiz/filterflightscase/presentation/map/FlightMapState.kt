package com.alihiz.filterflightscase.presentation.map

import com.alihiz.filterflightscase.core.constants.AppConstants.Display.MAX_FLIGHTS_TO_DISPLAY
import com.alihiz.filterflightscase.domain.model.Flight

data class FlightMapState(
    val flights: List<Flight> = emptyList(),
    val filteredFlights: List<Flight> = emptyList(),
    val availableCountries: List<String> = emptyList(),
    val selectedCountry: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null,
    val lastRefreshTime: Long = 0L
) {
    fun getDisplayFlights(): List<Flight> {
        val flightsToShow = filteredFlights.ifEmpty { flights }
        // Limit flights to prevent crash - take closest/highest altitude flights
        val max = MAX_FLIGHTS_TO_DISPLAY
        return if (flightsToShow.size > max) {
            flightsToShow
                .sortedByDescending { it.baroAltitude ?: 0.0 }
                .take(max)
        } else {
            flightsToShow
        }
    }

    fun hasFlights(): Boolean = getDisplayFlights().isNotEmpty()

    fun getFlightCount(): Int {
        val total = getTotalFlightCount()
        val max = MAX_FLIGHTS_TO_DISPLAY
        return total.coerceAtMost(max)
    }

    fun getTotalFlightCount(): Int = filteredFlights.ifEmpty { flights }.size

    fun isLimited(): Boolean = getTotalFlightCount() > MAX_FLIGHTS_TO_DISPLAY
}

