package com.alihiz.filterflightscase.data.di

import com.alihiz.filterflightscase.data.remote.api.OpenSkyApi
import com.alihiz.filterflightscase.data.repository.FlightRepositoryImpl
import com.alihiz.filterflightscase.domain.repository.FlightRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun bindFlightRepository(
        api: OpenSkyApi,
        @CoroutineDispatcherModule.DispatcherIO
        ioDispatcher: CoroutineDispatcher
    ): FlightRepository {
        return FlightRepositoryImpl(api = api, ioDispatcher = ioDispatcher)
    }
}

