package com.alihiz.filterflightscase.core.model

sealed interface ApiResult<out T> {
    class Success<out T>(val data: T) : ApiResult<T>
    class Error<out T>(val code: Int? = null, val message: String? = null) : ApiResult<T>
    class Exception<out T>(val throwable: Throwable) : ApiResult<T>
}

