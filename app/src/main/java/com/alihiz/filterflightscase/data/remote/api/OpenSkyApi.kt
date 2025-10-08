package com.alihiz.filterflightscase.data.remote.api

import com.alihiz.filterflightscase.data.remote.dto.FlightStateDto
import retrofit2.http.GET
import retrofit2.http.Query

// API documentation: https://openskynetwork.github.io/opensky-api/rest.html
interface OpenSkyApi {

    @GET("states/all")
    suspend fun getFlightsInBounds(
        @Query("lomin") lomin: Double,
        @Query("lamin") lamin: Double,
        @Query("lomax") lomax: Double,
        @Query("lamax") lamax: Double
    ): FlightStateDto
}
