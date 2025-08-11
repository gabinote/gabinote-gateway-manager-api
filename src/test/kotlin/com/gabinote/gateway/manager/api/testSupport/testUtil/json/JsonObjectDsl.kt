package com.gabinote.api.testSupport.testUtil.json

import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonNull
import com.google.gson.JsonObject

fun jsonBuilder(block: JsonObjectDsl.() -> Unit): String =
    JsonObjectDsl().apply(block).toJsonString()

// 객체 빌더
class JsonObjectDsl {
    private val obj = JsonObject()
    private val gson = Gson()

    /** 일반 값 추가 */
    infix fun String.to(value: Any?) {
        val element: JsonElement = when (value) {
            null -> JsonNull.INSTANCE
            is JsonElement -> value
            is Number, is Boolean, is String -> gson.toJsonTree(value)
            is JsonObjectDsl -> value.build()
            is JsonArrayDsl -> value.build()
            else -> gson.toJsonTree(value)
        }
        obj.add(this, element)
    }

    /** 중첩 객체 추가: "user" obj { ... } */
    fun String.obj(block: JsonObjectDsl.() -> Unit) {
        val child = JsonObjectDsl().apply(block).build()
        obj.add(this, child)
    }

    /** 배열 추가: "tags" arr { +"a"; +"b" } */
    fun String.arr(block: JsonArrayDsl.() -> Unit) {
        val child = JsonArrayDsl().apply(block).build()
        obj.add(this, child)
    }

    internal fun build(): JsonObject = obj

    fun toJsonString(): String = gson.toJson(obj)
}

