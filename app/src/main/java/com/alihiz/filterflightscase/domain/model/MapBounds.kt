package com.alihiz.filterflightscase.domain.model

data class MapBounds(
    val minLat: Double,
    val maxLat: Double,
    val minLon: Double,
    val maxLon: Double
) {
    init {
        require(minLat <= maxLat) { "minLat must be <= maxLat" }
        require(minLon <= maxLon) { "minLon must be <= maxLon" }
        require(minLat >= -90 && maxLat <= 90) { "Latitude must be between -90 and 90" }
        require(minLon >= -180 && maxLon <= 180) { "Longitude must be between -180 and 180" }
    }

    //Check if these bounds are valid for API queries.
    fun isValid(): Boolean {
        return minLat <= maxLat && minLon <= maxLon &&
                minLat >= -90 && maxLat <= 90 &&
                minLon >= -180 && maxLon <= 180
    }
}

