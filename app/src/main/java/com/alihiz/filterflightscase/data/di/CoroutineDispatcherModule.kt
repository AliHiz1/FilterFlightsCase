package com.alihiz.filterflightscase.data.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Qualifier
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoroutineDispatcherModule {

    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    annotation class DispatcherIO

    @Retention(AnnotationRetention.BINARY)
    @Qualifier
    annotation class DispatcherMain

    @Singleton
    @Provides
    @DispatcherIO
    fun providerDispatcherIO(): CoroutineDispatcher {
        return Dispatchers.IO
    }

    @Singleton
    @Provides
    @DispatcherMain
    fun providerDispatcherMain(): CoroutineDispatcher {
        return Dispatchers.Main
    }

}