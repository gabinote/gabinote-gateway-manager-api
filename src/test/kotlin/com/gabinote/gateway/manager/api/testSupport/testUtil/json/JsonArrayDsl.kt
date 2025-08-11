package com.gabinote.api.testSupport.testUtil.json

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonNull

// 배열 빌더
class JsonArrayDsl {
    private val arr = JsonArray()
    private val gson = Gson()

    /** 배열 요소 추가: +value */
    operator fun Any?.unaryPlus() {
        val element: JsonElement = when (this) {
            null -> JsonNull.INSTANCE
            is JsonElement -> this
            is Number, is Boolean, is String -> gson.toJsonTree(this)
            is JsonObjectDsl -> this.build()
            is JsonArrayDsl -> this.build()
            else -> gson.toJsonTree(this)
        }
        arr.add(element)
    }

    internal fun build(): JsonArray = arr
}