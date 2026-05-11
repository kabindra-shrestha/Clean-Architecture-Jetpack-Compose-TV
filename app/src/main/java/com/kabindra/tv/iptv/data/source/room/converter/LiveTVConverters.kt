package com.kabindra.tv.iptv.data.source.room.converter

import androidx.room.TypeConverter
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json

class LiveTVConverters {
    private val json = Json { ignoreUnknownKeys = true }
    private val intListSerializer = ListSerializer(Int.serializer())

    @TypeConverter
    fun fromCategoryIds(value: List<Int>?): String? {
        return value?.let { json.encodeToString(intListSerializer, it) }
    }

    @TypeConverter
    fun toCategoryIds(value: String?): List<Int>? {
        return value
            ?.takeIf { it.isNotBlank() }
            ?.let { encodedValue ->
                runCatching { json.decodeFromString(intListSerializer, encodedValue) }.getOrNull()
            }
    }
}
