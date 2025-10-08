package com.alihiz.filterflightscase.data.repository

import com.alihiz.filterflightscase.core.model.ApiResult
import com.alihiz.filterflightscase.data.mapper.toDomain
import com.alihiz.filterflightscase.data.remote.api.OpenSkyApi
import com.alihiz.filterflightscase.domain.model.Flight
import com.alihiz.filterflightscase.domain.model.MapBounds
import com.alihiz.filterflightscase.domain.repository.FlightRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import com.alihiz.filterflightscase.core.constants.AppConstants.Cache.BOUNDS_TOLERANCE
import com.alihiz.filterflightscase.core.constants.AppConstants.Cache.FLIGHT_CACHE_DURATION_MS
import com.alihiz.filterflightscase.data.di.CoroutineDispatcherModule
import kotlinx.coroutines.CoroutineDispatcher

class FlightRepositoryImpl @Inject constructor(
    private val api: OpenSkyApi,
    @CoroutineDispatcherModule.DispatcherIO
    private val ioDispatcher: CoroutineDispatcher,
    ) : FlightRepository {
    
    private data class CacheEntry(
        val flights: List<Flight>,
        val timestamp: Long,
        val bounds: MapBounds
    )

    @Volatile // This Annotation has been added because of ACID principles.
    private var cache: CacheEntry? = null

    override fun clearCache() {
        cache = null
    }
    
    override suspend fun getFlightsInBounds(bounds: MapBounds): ApiResult<List<Flight>> {
        return withContext(ioDispatcher) {
            try {
                if (!bounds.isValid()) {
                    return@withContext ApiResult.Exception<List<Flight>>(IllegalArgumentException("Invalid map bounds"))
                }
                
                // Check cache
                cache?.let { cacheEntry ->
                    val now = System.currentTimeMillis()
                    val cacheAge = now - cacheEntry.timestamp
                    
                    if (cacheAge < FLIGHT_CACHE_DURATION_MS && areBoundsSimilar(bounds, cacheEntry.bounds)) {
                        return@withContext ApiResult.Success(cacheEntry.flights)
                    }
                }

                val response = api.getFlightsInBounds(
                    lomin = bounds.minLon,
                    lamin = bounds.minLat,
                    lomax = bounds.maxLon,
                    lamax = bounds.maxLat
                )
                
                val flightDtos = response.toFlightDtoList()
                val flights = flightDtos.toDomain()

                cache = CacheEntry(
                    flights = flights,
                    timestamp = System.currentTimeMillis(),
                    bounds = bounds
                )
                
                ApiResult.Success(flights)
            } catch (e: Exception) {
                ApiResult.Exception(e)
            }
        }
    }
    
    private fun areBoundsSimilar(bounds1: MapBounds, bounds2: MapBounds): Boolean {
        return kotlin.math.abs(bounds1.minLat - bounds2.minLat) < BOUNDS_TOLERANCE &&
               kotlin.math.abs(bounds1.maxLat - bounds2.maxLat) < BOUNDS_TOLERANCE &&
               kotlin.math.abs(bounds1.minLon - bounds2.minLon) < BOUNDS_TOLERANCE &&
               kotlin.math.abs(bounds1.maxLon - bounds2.maxLon) < BOUNDS_TOLERANCE
    }
}
