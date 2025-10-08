package com.alihiz.filterflightscase.data.mapper

import com.alihiz.filterflightscase.data.remote.dto.FlightDto
import com.alihiz.filterflightscase.domain.model.Flight

fun FlightDto.toDomain(): Flight {
    return Flight(
        icao24 = icao24,
        callsign = callsign,
        originCountry = originCountry,
        timePosition = timePosition,
        lastContact = lastContact,
        longitude = longitude,
        latitude = latitude,
        baroAltitude = baroAltitude,
        onGround = onGround,
        velocity = velocity,
        trueTrack = trueTrack,
        verticalRate = verticalRate,
        sensors = sensors,
        geoAltitude = geoAltitude,
        squawk = squawk,
        spi = spi,
        positionSource = positionSource
    )
}

fun List<FlightDto>.toDomain(): List<Flight> {
    return map { it.toDomain() }
}

