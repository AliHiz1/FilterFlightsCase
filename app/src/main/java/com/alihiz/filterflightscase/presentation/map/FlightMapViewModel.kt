package com.alihiz.filterflightscase.presentation.map

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.alihiz.filterflightscase.R
import com.alihiz.filterflightscase.core.model.ApiResult
import com.alihiz.filterflightscase.domain.model.MapBounds
import com.alihiz.filterflightscase.domain.usecase.FilterFlightsByCountryUseCase
import com.alihiz.filterflightscase.domain.usecase.GetAvailableCountriesUseCase
import com.alihiz.filterflightscase.domain.usecase.GetFlightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import com.alihiz.filterflightscase.core.constants.AppConstants.Refresh.AUTO_REFRESH_DELAY_MS
import com.alihiz.filterflightscase.core.constants.AppConstants.Refresh.DEBOUNCE_DELAY_MS
import com.alihiz.filterflightscase.core.constants.AppConstants.Refresh.MAX_CONSECUTIVE_ERRORS
import com.alihiz.filterflightscase.core.constants.AppConstants.Refresh.MIN_REQUEST_INTERVAL_MS
import com.alihiz.filterflightscase.data.di.CoroutineDispatcherModule
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

@HiltViewModel
class FlightMapViewModel @Inject constructor(
    private val getFlightsUseCase: GetFlightsUseCase,
    private val getAvailableCountriesUseCase: GetAvailableCountriesUseCase,
    private val filterFlightsByCountryUseCase: FilterFlightsByCountryUseCase,
    @ApplicationContext private val applicationContext: Context,
    @CoroutineDispatcherModule.DispatcherIO private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private val _state = MutableStateFlow(FlightMapState())
    val state: StateFlow<FlightMapState> = _state.asStateFlow()

    private var currentBounds: MapBounds? = null

    private var autoRefreshJob: Job? = null
    private var debounceJob: Job? = null
    private var activeFetchJob: Job? = null

    private var lastFetchTime = 0L
    private var consecutiveErrors = 0

    fun fetchFlights(bounds: MapBounds) {
        debounceJob?.cancel()
        
        debounceJob = viewModelScope.launch(ioDispatcher) {
            delay(DEBOUNCE_DELAY_MS)
            
            val now = System.currentTimeMillis()
            val timeSinceLastFetch = now - lastFetchTime
            
            if (timeSinceLastFetch < MIN_REQUEST_INTERVAL_MS) {
                return@launch
            }
            
            if (currentBounds != bounds) {
                autoRefreshJob?.cancel()
                currentBounds = bounds
                if (consecutiveErrors < MAX_CONSECUTIVE_ERRORS) {
                    startAutoRefresh(bounds)
                }
            }

            _state.update { it.copy(isLoading = true, error = null) }
            lastFetchTime = now

            // Ensure only one collector runs at a time
            activeFetchJob?.cancel()
            activeFetchJob = viewModelScope.launch(ioDispatcher) {
                try {
                    getFlightsUseCase(bounds).collect { result ->
                        when (result) {
                            is ApiResult.Success -> {
                                consecutiveErrors = 0
                                val flights = result.data

                                val countries = when (val countriesResult = getAvailableCountriesUseCase(flights)) {
                                    is ApiResult.Success -> countriesResult.data
                                    is ApiResult.Error -> emptyList()
                                    is ApiResult.Exception -> emptyList()
                                }

                                val filtered = filterFlightsByCountryUseCase(
                                    flights = flights,
                                    country = _state.value.selectedCountry
                                )

                                _state.update {
                                    it.copy(
                                        flights = flights,
                                        filteredFlights = filtered,
                                        availableCountries = countries,
                                        isLoading = false,
                                        error = null,
                                        lastRefreshTime = System.currentTimeMillis()
                                    )
                                }
                            }
                            is ApiResult.Error -> {
                                consecutiveErrors++
                                if ((result.message ?: "").contains("429")) {
                                    lastFetchTime = System.currentTimeMillis() + 30_000L
                                }
                                if (consecutiveErrors >= MAX_CONSECUTIVE_ERRORS) {
                                    autoRefreshJob?.cancel()
                                }
                                val errorMessage = result.message ?: applicationContext.getString(R.string.unknown_error)
                                _state.update { it.copy(isLoading = false, error = errorMessage) }
                            }
                            is ApiResult.Exception -> {
                                consecutiveErrors++
                                val errorMessage = formatError(throwable = result.throwable)
                                _state.update { it.copy(isLoading = false, error = errorMessage) }
                            }
                        }
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, error = formatError(throwable = e)) }
                }
            }
        }
    }

    private fun formatError(code: Int? = null, message: String? = null, throwable: Throwable? = null): String {
        val msg = message ?: throwable?.message
        val m = msg?.lowercase()
        if (code == 429 || m?.contains("429") == true) return applicationContext.getString(R.string.rate_limit_exceeded_error)
        if (code == 401 || m?.contains("401") == true) return applicationContext.getString(R.string.authentication_failed_error)
        if (m?.contains("timeout") == true || (throwable is SocketTimeoutException)) return applicationContext.getString(R.string.connection_timeout_error)
        if (throwable is UnknownHostException) return applicationContext.getString(R.string.no_internet_connection_error)

        // Include code in message if available for visibility
        return when {
            code != null && !msg.isNullOrBlank() -> "HTTP $code: $msg"
            code != null -> "HTTP $code"
            !msg.isNullOrBlank() -> msg
            else -> applicationContext.getString(R.string.unknown_error)
        }
    }

    private fun startAutoRefresh(bounds: MapBounds) {
        autoRefreshJob = viewModelScope.launch(ioDispatcher) {
            while (isActive) {
                delay(AUTO_REFRESH_DELAY_MS)
                if (currentBounds == bounds) {
                    fetchFlights(bounds)
                }
            }
        }
    }

    fun filterByCountry(country: String?) {
        _state.update { currentState ->
            val filtered = filterFlightsByCountryUseCase(
                flights = currentState.flights,
                country = country
            )
            currentState.copy(
                selectedCountry = country,
                filteredFlights = filtered
            )
        }
    }

    fun clearFilter() {
        filterByCountry(null)
    }

    fun refresh() {
        currentBounds?.let { bounds ->
            debounceJob?.cancel()
            autoRefreshJob?.cancel()
            activeFetchJob?.cancel()
            
            consecutiveErrors = 0
            lastFetchTime = 0L
            
            _state.update { it.copy(isLoading = true, error = null) }
            
            activeFetchJob = viewModelScope.launch(ioDispatcher) {
                lastFetchTime = System.currentTimeMillis()

                val flow = try {
                    getFlightsUseCase.forceRefresh(bounds)
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, error = formatError(throwable = e)) }
                    return@launch
                }

                try {
                    flow.collect { result ->
                        when (result) {
                            is ApiResult.Success -> {
                                consecutiveErrors = 0
                                val flights = result.data
                            val countries = when (val countriesResult = getAvailableCountriesUseCase(flights)) {
                                is ApiResult.Success -> countriesResult.data
                                is ApiResult.Error -> emptyList()
                                is ApiResult.Exception -> emptyList()
                                }
                                val filtered = filterFlightsByCountryUseCase(
                                    flights = flights,
                                    country = _state.value.selectedCountry
                                )
                                _state.update {
                                    it.copy(
                                        flights = flights,
                                        filteredFlights = filtered,
                                        availableCountries = countries,
                                        isLoading = false,
                                        error = null,
                                        lastRefreshTime = System.currentTimeMillis()
                                    )
                                }
                            }
                            is ApiResult.Error -> {
                                consecutiveErrors++
                                val errorMessage = formatError(code = result.code, message = result.message)
                                _state.update { it.copy(isLoading = false, error = errorMessage) }
                            }
                            is ApiResult.Exception -> {
                                consecutiveErrors++
                                val errorMessage = formatError(throwable = result.throwable)
                                _state.update { it.copy(isLoading = false, error = errorMessage) }
                            }
                        }
                    }
                } catch (e: Exception) {
                    _state.update { it.copy(isLoading = false, error = formatError(throwable = e)) }
                }

                if (consecutiveErrors < MAX_CONSECUTIVE_ERRORS) {
                    currentBounds = bounds
                    startAutoRefresh(bounds)
                }
            }
        }
    }

    fun clearError() {
        _state.update { it.copy(error = null) }
    }

    override fun onCleared() {
        super.onCleared()
        autoRefreshJob?.cancel()
        debounceJob?.cancel()
        activeFetchJob?.cancel()
    }
}
