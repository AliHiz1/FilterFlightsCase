package com.alihiz.filterflightscase.data.remote.api

import com.alihiz.filterflightscase.data.remote.dto.OAuth2TokenDto
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface OpenSkyAuthApi {

    @FormUrlEncoded
    @POST("auth/realms/opensky-network/protocol/openid-connect/token")
    suspend fun getToken(
        @Field("grant_type") grantType: String = "client_credentials",
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String
    ): OAuth2TokenDto
}

