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

private const val TEMPLATE_STRING = "REPLACE"
private const val TEMPLATE_DATE = "1970-01-01T00:00:00Z"
private const val TEMPLATE_DURATION = "9999999999s"
private const val TEMPLATE_NUMBER = "9999999999"

inline fun <reified T> replaceTemplateString(expected: T, response: T, path: String = "$."): T {
    val expectedJson = Json.encodeToJsonElement(expected)
    val responseJson = Json.encodeToJsonElement(response)
    return Json.decodeFromJsonElement(replaceTemplateString(expectedJson, responseJson, path))
}

fun replaceTemplateString(expected: JsonElement, response: JsonElement, path: String): JsonElement {
    if (response is JsonNull) {
        return response
    }
    check(expected !is JsonNull) { "'expected' must not be null at JSON path: $path" }

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
    return if (
        expectedValue == TEMPLATE_STRING ||
        expectedValue == TEMPLATE_DATE ||
        expectedValue == TEMPLATE_DURATION ||
        expectedValue == TEMPLATE_NUMBER
    ) {
        response
    } else {
        expected
    }
}
