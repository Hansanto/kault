package com.github.hansanto.kault.extension

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.exception.VaultFieldNotFoundException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject

/**
 * Represents the separator used in URLs.
 *
 * This constant defines the string value "/" which is commonly used as a separator in URLs.
 */
public const val URL_SEPARATOR: String = "/"

/**
 * Decodes the response body as a JSON object and returns the value of the specified field.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @return Decoded value of the specified field.
 */
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldObject(format: Json, fieldName: String): T {
    return decodeBodyJsonFieldObjectOrNull(format, fieldName) ?: throw VaultFieldNotFoundException(fieldName)
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
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldObjectOrNull(format: Json, fieldName: String): T? {
    val data = getBodyJsonElementOrNull(fieldName)?.jsonObject ?: return null
    return format.decodeFromJsonElement<T>(data)
}

/**
 * Decodes the response body as a JSON array and returns the value of the specified field as a list.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @return List of decoded values of the specified field.
 */
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldArray(format: Json, fieldName: String): List<T> {
    return decodeBodyJsonFieldArrayOrNull(format, fieldName) ?: throw VaultFieldNotFoundException(fieldName)
}

/**
 * Decodes the response body as a JSON array and returns the value of the specified field as a list.
 * If the body is null or the field is not found, null is returned.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @return List of decoded values of the specified field.
 */
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldArrayOrNull(format: Json, fieldName: String): List<T>? {
    val data = getBodyJsonElementOrNull(fieldName)?.jsonArray ?: return null
    return format.decodeFromJsonElement<List<T>>(data)
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
