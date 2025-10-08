package com.alihiz.filterflightscase.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class OAuth2TokenDto(
    @Json(name = "access_token")
    val accessToken: String,
    
    @Json(name = "expires_in")
    val expiresIn: Int,
    
    @Json(name = "token_type")
    val tokenType: String,
    
    @Json(name = "refresh_expires_in")
    val refreshExpiresIn: Int? = null,
    
    @Json(name = "scope")
    val scope: String? = null
)

