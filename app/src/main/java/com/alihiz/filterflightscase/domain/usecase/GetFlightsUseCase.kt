package com.alihiz.filterflightscase.domain.usecase

import com.alihiz.filterflightscase.core.model.ApiResult
import com.alihiz.filterflightscase.domain.model.Flight
import com.alihiz.filterflightscase.domain.model.MapBounds
import com.alihiz.filterflightscase.domain.repository.FlightRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import javax.inject.Inject

class GetFlightsUseCase @Inject constructor(
    private val repository: FlightRepository
) {
    operator fun invoke(bounds: MapBounds): Flow<ApiResult<List<Flight>>> = flow {
        val result = repository.getFlightsInBounds(bounds)
        emit(result)
        when (result) {
            is ApiResult.Exception -> throw result.throwable
            is ApiResult.Error -> throw IllegalStateException(result.message ?: "Api error")
            else -> {}
        }
    }
    .retry(retries = 2) { cause -> // We limit resource usage by defining a fixed number of retries
        val shouldRetry = cause is java.net.UnknownHostException ||
                         cause is java.net.SocketTimeoutException ||
                         cause.message?.contains("timeout", ignoreCase = true) == true
        
        if (shouldRetry) {
            delay(1000)
        }
        shouldRetry
    }
    .catch { e -> emit(ApiResult.Exception(e)) }

    fun forceRefresh(bounds: MapBounds): Flow<ApiResult<List<Flight>>> = flow {
        repository.clearCache()
        val result = repository.getFlightsInBounds(bounds)
        emit(result)
        when (result) {
            is ApiResult.Exception -> throw result.throwable
            is ApiResult.Error -> throw IllegalStateException(result.message ?: "Api error")
            is ApiResult.Success -> {}
        }
    }
    .catch { e -> emit(ApiResult.Exception(e)) }
}
