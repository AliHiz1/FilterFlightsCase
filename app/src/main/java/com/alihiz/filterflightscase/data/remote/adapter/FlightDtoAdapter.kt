package com.alihiz.filterflightscase.data.remote.adapter

import com.alihiz.filterflightscase.data.remote.dto.FlightDto
import com.alihiz.filterflightscase.core.json.ArrayMappingAdapter
import com.alihiz.filterflightscase.core.json.getBoolean
import com.alihiz.filterflightscase.core.json.getDouble
import com.alihiz.filterflightscase.core.json.getInt
import com.alihiz.filterflightscase.core.json.getLong
import com.alihiz.filterflightscase.core.json.getString

 // OpenSky API returns flights as arrays (not objects) for bandwidth optimization.
 // This adapter maps array indices to FlightDto fields based on OpenSky API specification.
class FlightDtoAdapter : ArrayMappingAdapter<FlightDto>() {
    
    companion object {
        // OpenSky API array indices (documented in API spec)
        private const val INDEX_ICAO24 = 0
        private const val INDEX_CALLSIGN = 1
        private const val INDEX_ORIGIN_COUNTRY = 2
        private const val INDEX_TIME_POSITION = 3
        private const val INDEX_LAST_CONTACT = 4
        private const val INDEX_LONGITUDE = 5
        private const val INDEX_LATITUDE = 6
        private const val INDEX_BARO_ALTITUDE = 7
        private const val INDEX_ON_GROUND = 8
        private const val INDEX_VELOCITY = 9
        private const val INDEX_TRUE_TRACK = 10
        private const val INDEX_VERTICAL_RATE = 11
        private const val INDEX_SENSORS = 12
        private const val INDEX_GEO_ALTITUDE = 13
        private const val INDEX_SQUAWK = 14
        private const val INDEX_SPI = 15
        private const val INDEX_POSITION_SOURCE = 16
        private const val MIN_ARRAY_SIZE = 17
    }
    
    override fun mapArray(array: List<Any?>): FlightDto? {
        if (array.size < MIN_ARRAY_SIZE) return null
        
        // Required fields - return null if missing
        val icao24 = array.getString(INDEX_ICAO24) ?: return null
        val originCountry = array.getString(INDEX_ORIGIN_COUNTRY) ?: return null
        
        // Map all fields using type-safe extension functions
        return FlightDto(
            icao24 = icao24,
            callsign = array.getString(INDEX_CALLSIGN),
            originCountry = originCountry,
            timePosition = array.getLong(INDEX_TIME_POSITION),
            lastContact = array.getLong(INDEX_LAST_CONTACT),
            longitude = array.getDouble(INDEX_LONGITUDE),
            latitude = array.getDouble(INDEX_LATITUDE),
            baroAltitude = array.getDouble(INDEX_BARO_ALTITUDE),
            onGround = array.getBoolean(INDEX_ON_GROUND, default = false),
            velocity = array.getDouble(INDEX_VELOCITY),
            trueTrack = array.getDouble(INDEX_TRUE_TRACK),
            verticalRate = array.getDouble(INDEX_VERTICAL_RATE),
            sensors = (array.getOrNull(INDEX_SENSORS) as? List<*>)?.mapNotNull { (it as? Number)?.toInt() },
            geoAltitude = array.getDouble(INDEX_GEO_ALTITUDE),
            squawk = array.getString(INDEX_SQUAWK),
            spi = array.getBoolean(INDEX_SPI, default = false),
            positionSource = array.getInt(INDEX_POSITION_SOURCE) ?: 0
        )
    }
}

