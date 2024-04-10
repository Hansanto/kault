package io.github.hansanto.kault.util

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

private const val TEMPLATE_STRING = "REPLACED_DYNAMICALLY"
private const val TEMPLATE_DATE = "1970-01-01T00:00:00Z"
private const val TEMPLATE_DURATION = "999d"

inline fun <reified T> replaceTemplateString(expected: T, response: T): T {
    val expectedJson = Json.encodeToJsonElement(expected)
    val responseJson = Json.encodeToJsonElement(response)
    return Json.decodeFromJsonElement(replaceTemplateString(expectedJson, responseJson))
}

fun replaceTemplateString(expected: JsonElement, response: JsonElement): JsonElement {
    if (response is JsonNull) {
        return response
    }
    if (expected is JsonPrimitive) {
        return replaceTemplateString(expected.jsonPrimitive, response.jsonPrimitive)
    }
    if (expected is JsonArray) {
        return replaceTemplateString(expected.jsonArray, response.jsonArray)
    }
    if (expected is JsonObject) {
        return replaceTemplateString(expected.jsonObject, response.jsonObject)
    }
    throw IllegalArgumentException("Expected and response must be of type JsonArray or JsonObject")
}

fun replaceTemplateString(
    expectedJson: JsonObject,
    responseJson: JsonObject
): JsonElement {
    val replacedExpectedJsonObject = expectedJson.mapValues { (key, value) ->
        replaceTemplateString(value, responseJson[key] ?: JsonNull)
    }
    return JsonObject(replacedExpectedJsonObject)
}

fun replaceTemplateString(
    expectedJson: JsonArray,
    responseJson: JsonArray
): JsonArray {
    val replacedExpectedJsonArray = expectedJson.mapIndexed { index, value ->
        replaceTemplateString(value, responseJson[index])
    }
    return JsonArray(replacedExpectedJsonArray)
}

fun replaceTemplateString(
    expected: JsonPrimitive,
    response: JsonPrimitive
): JsonPrimitive {
    val expectedValue = expected.content
    return if (
        expectedValue == TEMPLATE_STRING ||
        expectedValue == TEMPLATE_DATE ||
        expectedValue == TEMPLATE_DURATION
    ) {
        response
    } else {
        expected
    }
}
