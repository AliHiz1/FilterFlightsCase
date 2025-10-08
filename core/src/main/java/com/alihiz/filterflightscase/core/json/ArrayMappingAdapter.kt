package com.alihiz.filterflightscase.core.json

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import java.io.IOException

abstract class ArrayMappingAdapter<T> : JsonAdapter<T>() {
    abstract fun mapArray(array: List<Any?>): T?

    @Throws(IOException::class)
    override fun fromJson(reader: JsonReader): T? {
        if (reader.peek() != JsonReader.Token.BEGIN_ARRAY) {
            reader.skipValue()
            return null
        }
        val array = mutableListOf<Any?>()
        reader.beginArray()
        while (reader.hasNext()) {
            when (reader.peek()) {
                JsonReader.Token.BOOLEAN -> array.add(reader.nextBoolean())
                JsonReader.Token.NUMBER -> array.add(reader.nextDouble())
                JsonReader.Token.STRING -> array.add(reader.nextString())
                JsonReader.Token.NULL -> {
                    reader.nextNull<Any>()
                    array.add(null)
                }
                else -> {
                    reader.skipValue()
                    array.add(null)
                }
            }
        }
        reader.endArray()
        return mapArray(array)
    }

    override fun toJson(writer: JsonWriter, value: T?) {
        throw UnsupportedOperationException("toJson not implemented for ArrayMappingAdapter")
    }
}

fun List<Any?>.getString(index: Int): String? = this.getOrNull(index) as? String
fun List<Any?>.getDouble(index: Int): Double? = (this.getOrNull(index) as? Number)?.toDouble()
fun List<Any?>.getInt(index: Int): Int? = (this.getOrNull(index) as? Number)?.toInt()
fun List<Any?>.getLong(index: Int): Long? = (this.getOrNull(index) as? Number)?.toLong()
fun List<Any?>.getBoolean(index: Int, default: Boolean = false): Boolean = this.getOrNull(index) as? Boolean ?: default


