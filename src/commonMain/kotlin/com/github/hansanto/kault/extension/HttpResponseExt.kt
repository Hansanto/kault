package com.github.hansanto.kault.extension

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.exception.VaultFieldNotFoundException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonObject

/**
 * Decodes the response body as a JSON object and returns the value of the specified field.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @return Decoded value of the specified field.
 */
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonField(format: Json, fieldName: String): T {
    return decodeBodyJsonFieldOrNull(format, fieldName) ?: throw VaultFieldNotFoundException(fieldName)
}

/**
 * Decodes the response body as a JSON object and returns the value of the specified field.
 * If the body is null or the field is not found, null is returned.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @return Decoded value of the specified field.
 */
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldOrNull(format: Json, fieldName: String): T? {
    val data = getBodyJsonObjectOrNull(fieldName) ?: return null
    return format.decodeFromJsonElement<T>(data)
}

/**
 * Retrieves the value of a specified JSON field from the response body and returns it as a JsonObject.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @return JsonObject representing the value of the specified field.
 * @throws VaultFieldNotFoundException if the specified field is not found in the JSON object.
 */
internal suspend fun HttpResponse.getBodyJsonObjectOrNull(fieldName: String): JsonObject? {
    return getBodyJsonElementOrNull(fieldName)?.jsonObject
}

/**
 * Retrieves the value of a specified JSON field from the response body and returns it as a JsonElement.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @return JsonElement representing the value of the specified field.
 * @throws VaultFieldNotFoundException if the specified field is not found in the JSON object.
 */
internal suspend fun HttpResponse.getBodyJsonElementOrNull(fieldName: String): JsonElement? {
    val text = bodyAsText()
    if (text.isEmpty()) {
        return null
    }
    return VaultClient.json.parseToJsonElement(text).jsonObject[fieldName]
}
