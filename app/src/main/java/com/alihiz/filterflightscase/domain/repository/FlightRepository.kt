package com.alihiz.filterflightscase.domain.repository

import com.alihiz.filterflightscase.core.model.ApiResult
import com.alihiz.filterflightscase.domain.model.Flight
import com.alihiz.filterflightscase.domain.model.MapBounds

interface FlightRepository {
    suspend fun getFlightsInBounds(bounds: MapBounds): ApiResult<List<Flight>>
    fun clearCache()
}
