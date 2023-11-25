package io.github.hansanto.kault.extension

import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.exception.VaultFieldNotFoundException
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Represents the separator used in URLs.
 *
 * This constant defines the string value "/" which is commonly used as a separator in URLs.
 */
public const val URL_PATH_SEPARATOR: String = "/"

/**
 * Decodes the response body as a JSON object and returns the value of the specified field.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @return Decoded value of the specified field.
 */
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldObject(fieldName: String, format: Json): T {
    return decodeBodyJsonFieldObject(
        format,
        { it[fieldName]?.jsonObject },
        { throw VaultAPIException(listOf("The API didn't provide a response body.")) },
        { throw VaultFieldNotFoundException(fieldName) }
    )
}

/**
 * Decodes the response body as a JSON array and returns the value of the specified field as a list.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @return List of decoded values of the specified field.
 */
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldArray(
    fieldName: String,
    format: Json
): List<T> {
    return decodeBodyJsonFieldObject(
        format,
        { it[fieldName]?.jsonArray },
        { throw VaultAPIException(listOf("The API didn't provide a response body.")) },
        { throw VaultFieldNotFoundException(fieldName) }
    )
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
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldObjectOrNull(
    fieldName: String,
    format: Json
): T? {
    return decodeBodyJsonFieldObject(
        format,
        { it[fieldName]?.jsonObject },
        { null },
        { null }
    )
}

private suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldObject(
    format: Json,
    getJsonField: (JsonObject) -> JsonElement?,
    onEmptyBody: () -> T,
    onFieldNotFound: () -> T
): T {
    val text = bodyAsText()
    if (text.isEmpty()) {
        return onEmptyBody()
    }

    val jsonBody = format.parseToJsonElement(text).jsonObject
    val data = getJsonField(jsonBody)
    if (data == null) {
        // If the expected field is not found, try to find an error message in the response body
        // If an error message is found, throw a VaultAPIException
        // Otherwise, throw a VaultFieldNotFoundException to indicate that the field was not found
        val errors = findErrorFromVaultResponseBody(jsonBody) ?: return onFieldNotFound()
        throw VaultAPIException(errors)
    }

    return format.decodeFromJsonElement<T>(data)
}

/**
 * Find an error message from Vault response body.
 * If the fields "errors" or "data.error" are found, return the list of errors.
 * Otherwise, return null.
 * @param jsonBody Json object representing the response body.
 * @return List of errors if found, null otherwise.
 */
public fun findErrorFromVaultResponseBody(jsonBody: JsonObject): List<String>? {
    /**
     * {
     *  "errors": [
     *    "error1",
     *    "error2"
     *    ...
     *  ]
     * }
     */
    jsonBody["errors"]?.jsonArray?.let { array ->
        return array.map { it.jsonPrimitive.content }
    }

    /**
     * {
     *  "data": {
     *   "error": "error1"
     *  }
     * }
     */
    jsonBody["data"]?.jsonObject?.get("error")?.jsonPrimitive?.content?.let {
        return listOf(it)
    }

    return null
}
