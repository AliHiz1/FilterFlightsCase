package com.alihiz.filterflightscase.data.remote.auth

import com.alihiz.filterflightscase.BuildConfig
import com.alihiz.filterflightscase.core.constants.AppConstants.Refresh.TOKEN_REFRESH_BUFFER_MS
import com.alihiz.filterflightscase.data.remote.api.OpenSkyAuthApi
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    private val authApi: OpenSkyAuthApi
) {
    private var cachedToken: String? = null
    private var tokenExpiryTime: Long = 0L
    private val mutex = Mutex()

    suspend fun getValidToken(): String? {
        mutex.withLock {
            val clientId = BuildConfig.OPENSKY_CLIENT_ID
            val clientSecret = BuildConfig.OPENSKY_CLIENT_SECRET

            // If no OAuth2 credentials, return null (will use anonymous)
            if (clientId.isBlank() || clientSecret.isBlank()) {
                return null
            }

            val now = System.currentTimeMillis()
            if (cachedToken != null && now < tokenExpiryTime) {
                return cachedToken
            }

            return try {
                val response = authApi.getToken(
                    clientId = clientId,
                    clientSecret = clientSecret
                )
                cachedToken = response.accessToken
                tokenExpiryTime = now + (response.expiresIn * 1000L) - TOKEN_REFRESH_BUFFER_MS
                cachedToken
            } catch (e: Exception) {
                null
            }
        }
    }

    // Clear cached token after logout (if needed).
    fun clearToken() {
        cachedToken = null
        tokenExpiryTime = 0L
    }
}