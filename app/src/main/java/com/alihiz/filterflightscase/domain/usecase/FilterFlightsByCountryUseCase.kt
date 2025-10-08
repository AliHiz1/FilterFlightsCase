package com.alihiz.filterflightscase.domain.usecase

import com.alihiz.filterflightscase.domain.model.Flight
import javax.inject.Inject

class FilterFlightsByCountryUseCase @Inject constructor() {
    operator fun invoke(flights: List<Flight>, country: String?): List<Flight> {
        return if (country.isNullOrBlank()) {
            flights
        } else {
            flights.filter { it.originCountry.equals(country, ignoreCase = true) }
        }
    }
}

