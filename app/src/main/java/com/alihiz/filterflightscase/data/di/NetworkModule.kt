package com.alihiz.filterflightscase.data.di

import com.alihiz.filterflightscase.BuildConfig
import com.alihiz.filterflightscase.core.constants.AppConstants.Network.API_BASE_URL
import com.alihiz.filterflightscase.core.constants.AppConstants.Network.AUTH_BASE_URL
import com.alihiz.filterflightscase.core.constants.AppConstants.Network.TIMEOUT_SECONDS
import com.alihiz.filterflightscase.data.remote.api.OpenSkyApi
import com.alihiz.filterflightscase.data.remote.api.OpenSkyAuthApi
import com.alihiz.filterflightscase.data.remote.auth.TokenManager
import com.squareup.moshi.Moshi
import com.alihiz.filterflightscase.core.json.ArrayMappingAdapterFactory
import com.alihiz.filterflightscase.data.remote.adapter.FlightDtoAdapter
import com.alihiz.filterflightscase.data.remote.dto.FlightDto
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.runBlocking
import okhttp3.Credentials
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class AuthRetrofit
    
    @Qualifier
    @Retention(AnnotationRetention.BINARY)
    annotation class ApiRetrofit

    @Provides
    @Singleton
    fun provideMoshi(): Moshi {
        val factory = ArrayMappingAdapterFactory(
            mapOf(
                FlightDto::class.java to FlightDtoAdapter()
            )
        )
        return Moshi.Builder()
            .add(factory)
            .add(KotlinJsonAdapterFactory())
            .build()
    }

    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @AuthRetrofit
    fun provideAuthRetrofit(
        @AuthRetrofit okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(AUTH_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenSkyAuthApi(@AuthRetrofit retrofit: Retrofit): OpenSkyAuthApi {
        return retrofit.create(OpenSkyAuthApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTokenManager(authApi: OpenSkyAuthApi): TokenManager {
        return TokenManager(authApi)
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(tokenManager: TokenManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            
            // Try OAuth2 first (new accounts)
            val clientId = BuildConfig.OPENSKY_CLIENT_ID
            val clientSecret = BuildConfig.OPENSKY_CLIENT_SECRET
            
            val requestBuilder = if (clientId.isNotBlank() && clientSecret.isNotBlank()) {
                // OAuth2: Get token and add Bearer header
                val token = runBlocking { tokenManager.getValidToken() }
                if (token != null) {
                    originalRequest.newBuilder()
                        .header("Authorization", "Bearer $token")
                } else {
                    // Fall back to anonymous if token retrieval fails
                    originalRequest.newBuilder()
                }
            } else {
                // Fallback to Basic Auth (legacy accounts)
                val username = BuildConfig.OPENSKY_USERNAME
                val password = BuildConfig.OPENSKY_PASSWORD
                
                if (username.isNotBlank() && password.isNotBlank()) {
                    val credentials = Credentials.basic(username, password)
                    originalRequest.newBuilder()
                        .header("Authorization", credentials)
                } else {
                    // No authentication (anonymous)
                    originalRequest.newBuilder()
                }
            }
            
            chain.proceed(requestBuilder.build())
        }
    }

    @Provides
    @Singleton
    @ApiRetrofit
    fun provideApiOkHttpClient(
        authInterceptor: Interceptor
    ): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        
        return OkHttpClient.Builder()
            .addInterceptor(authInterceptor) // Add auth first
            .addInterceptor(loggingInterceptor)
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    @ApiRetrofit
    fun provideApiRetrofit(
        @ApiRetrofit okHttpClient: OkHttpClient,
        moshi: Moshi
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    @Provides
    @Singleton
    fun provideOpenSkyApi(@ApiRetrofit retrofit: Retrofit): OpenSkyApi {
        return retrofit.create(OpenSkyApi::class.java)
    }
}

