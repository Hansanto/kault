package io.github.hansanto.kault.util

import io.kotest.matchers.ints.shouldBeExactly
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

private val templates = setOf(
    "REPLACE", // strings
    "1970-01-01T00:00:00Z", // dates
    "9999999999s", // durations
    "9999999999" // numbers
)

inline fun <reified T> replaceTemplateString(expected: T, response: T, path: String = "$."): T {
    val expectedJson = Json.encodeToJsonElement(expected)
    val responseJson = Json.encodeToJsonElement(response)
    return Json.decodeFromJsonElement(replaceTemplateString(expectedJson, responseJson, path))
}

fun replaceTemplateString(expected: JsonElement, response: JsonElement, path: String): JsonElement {
    if (response is JsonNull) {
        check(expected is JsonNull) { "{expected}$path is not null but {response}$path is null" }
        return response
    }

    check(expected !is JsonNull) { "{expected}$path is null but {response}$path is not null" }

    if (expected is JsonPrimitive) {
        return replaceTemplateString(expected.jsonPrimitive, response.jsonPrimitive)
    }
    if (expected is JsonArray) {
        return replaceTemplateString(expected.jsonArray, response.jsonArray, path)
    }
    if (expected is JsonObject) {
        return replaceTemplateString(expected.jsonObject, response.jsonObject, path)
    }
    throw IllegalArgumentException("Expected and response must be of type JsonArray or JsonObject")
}

fun replaceTemplateString(expectedJson: JsonObject, responseJson: JsonObject, path: String): JsonElement {
    val replacedExpectedJsonObject = expectedJson.mapValues { (key, value) ->
        replaceTemplateString(value, responseJson[key] ?: JsonNull, "$path$key")
    }
    return JsonObject(replacedExpectedJsonObject)
}

fun replaceTemplateString(expectedJson: JsonArray, responseJson: JsonArray, path: String): JsonArray {
    responseJson.size shouldBeExactly expectedJson.size
    val replacedExpectedJsonArray = expectedJson.mapIndexed { index, value ->
        replaceTemplateString(value, responseJson[index], "$path[$index]")
    }
    return JsonArray(replacedExpectedJsonArray)
}

fun replaceTemplateString(expected: JsonPrimitive, response: JsonPrimitive): JsonPrimitive {
    val expectedValue = expected.content
    return if (expectedValue in templates) {
        response
    } else {
        expected
    }
}
