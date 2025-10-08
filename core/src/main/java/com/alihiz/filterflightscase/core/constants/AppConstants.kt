package com.alihiz.filterflightscase.core.constants

object AppConstants {
    object Refresh {
        const val AUTO_REFRESH_DELAY_MS: Long = 10_000L
        const val DEBOUNCE_DELAY_MS: Long = 500L
        const val MIN_REQUEST_INTERVAL_MS: Long = 2_000L
        const val MAX_CONSECUTIVE_ERRORS: Int = 5
        const val TOKEN_REFRESH_BUFFER_MS = 60_000L
    }

    object Display {
        const val MAX_FLIGHTS_TO_DISPLAY: Int = 200
    }

    object Cache {
        const val FLIGHT_CACHE_DURATION_MS: Long = 10_000L
        const val BOUNDS_TOLERANCE: Double = 0.01
    }

    object Network {
        const val API_BASE_URL = "https://opensky-network.org/api/"
        const val AUTH_BASE_URL = "https://auth.opensky-network.org/"
        const val TIMEOUT_SECONDS = 30L
    }
}


