package com.alihiz.filterflightscase.domain.usecase

import com.alihiz.filterflightscase.core.model.ApiResult
import com.alihiz.filterflightscase.domain.model.Flight
import javax.inject.Inject

class GetAvailableCountriesUseCase @Inject constructor() {
    operator fun invoke(flights: List<Flight>): ApiResult<List<String>> {
        return try {
            val countries = flights
                .map { it.originCountry }
                .distinct()
                .sorted()
            ApiResult.Success(countries)
        } catch (e: Exception) {
            ApiResult.Exception(e)
        }
    }
}

