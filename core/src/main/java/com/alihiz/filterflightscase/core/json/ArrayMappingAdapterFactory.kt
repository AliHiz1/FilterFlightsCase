package com.alihiz.filterflightscase.core.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import java.lang.reflect.Type

class ArrayMappingAdapterFactory(
    private val adapters: Map<Type, ArrayMappingAdapter<*>>
) : JsonAdapter.Factory {
    override fun create(type: Type, annotations: Set<Annotation>, moshi: Moshi): JsonAdapter<*>? {
        return adapters[type]
    }
}


