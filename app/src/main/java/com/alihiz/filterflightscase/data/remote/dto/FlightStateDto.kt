package com.alihiz.filterflightscase.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class FlightStateDto(
    @Json(name = "time")
    val time: Long?,
    
    @Json(name = "states")
    val states: List<@JvmSuppressWildcards FlightDto>?
) {
    fun toFlightDtoList(): List<FlightDto> {
        return states?.filterNotNull() ?: emptyList()
    }
}

data class FlightDto(
    val icao24: String,
    val callsign: String?,
    val originCountry: String,
    val timePosition: Long?,
    val lastContact: Long?,
    val longitude: Double?,
    val latitude: Double?,
    val baroAltitude: Double?,
    val onGround: Boolean,
    val velocity: Double?,
    val trueTrack: Double?,
    val verticalRate: Double?,
    val sensors: List<Int>?,
    val geoAltitude: Double?,
    val squawk: String?,
    val spi: Boolean,
    val positionSource: Int
)

