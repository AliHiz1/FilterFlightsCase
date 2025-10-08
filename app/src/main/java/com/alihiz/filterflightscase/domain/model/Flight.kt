package com.alihiz.filterflightscase.domain.model

data class Flight(
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
) {
    fun hasValidPosition(): Boolean = latitude != null && longitude != null

    fun getDisplayName(): String = callsign?.takeIf { it.isNotBlank() } ?: icao24
}

