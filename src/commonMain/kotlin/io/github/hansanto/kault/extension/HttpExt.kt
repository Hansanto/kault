package io.github.hansanto.kault.extension

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.exception.VaultFieldNotFoundException
import io.ktor.client.HttpClient
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.request
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Represents the name of the field that contains the response data.
 */
private const val DATA_FIELD_RESPONSE = "data"

/**
 * Represents the name of the field that contains the response auth.
 */
private const val AUTH_FIELD_RESPONSE = "auth"

/**
 * Represents the separator used in URLs.
 *
 * This constant defines the string value "/" which is commonly used as a separator in URLs.
 */
public const val URL_PATH_SEPARATOR: String = "/"

/**
 * Represents the error message when the API doesn't provide a response body.
 */
private const val VAULT_API_ERROR_NO_BODY = "The API didn't provide a response body."

/**
 * Represents the HTTP method LIST.
 */
public val HttpMethod.Companion.List: HttpMethod
    get() = HttpMethod("LIST")

/**
 * Represents the content type "application/merge-patch+json".
 */
@Suppress("UnusedReceiverParameter")
public val ContentType.Application.MergePatchJson: ContentType
    get() = ContentType("application", "merge-patch+json")

/**
 * Executes an [HttpClient]'s LIST request with the parameters configured in [block].
 *
 * Learn more from [Making requests](https://ktor.io/docs/request.html).
 */
public suspend inline fun HttpClient.list(block: HttpRequestBuilder.() -> Unit): HttpResponse =
    list(HttpRequestBuilder().apply(block))

/**
 * Executes an [HttpClient]'s LIST request with the parameters configured in [builder].
 *
 * Learn more from [Making requests](https://ktor.io/docs/request.html).
 */
public suspend inline fun HttpClient.list(builder: HttpRequestBuilder): HttpResponse {
    builder.method = HttpMethod.List
    return request(builder)
}

/**
 * Decodes the response body as a JSON object and returns the value of the "auth" field.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @return Decoded value of the specified field.
 */
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonAuthFieldObject(format: Json = VaultClient.json): T {
    return decodeBodyJsonFieldObject(AUTH_FIELD_RESPONSE, format)
}

/**
 * Decodes the response body as a JSON object and returns the value of the "data" field.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @return Decoded value of the specified field.
 */
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonDataFieldObject(format: Json = VaultClient.json): T {
    return decodeBodyJsonFieldObject(DATA_FIELD_RESPONSE, format)
}

/**
 * Decodes the response body as a JSON object and returns the value of the "data" field.
 * If the body is null or the field is not found, null is returned.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @return Decoded value of the specified field.
 */
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonDataFieldObjectOrNull(format: Json = VaultClient.json): T? {
    return decodeBodyJsonFieldObjectOrNull(DATA_FIELD_RESPONSE, format)
}

/**
 * Decodes the response body as a JSON object and returns the value of the specified field.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @return Decoded value of the specified field.
 */
internal suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldObject(fieldName: String, format: Json = VaultClient.json): T {
    return decodeBodyJsonFieldObject(
        format,
        { it[fieldName]?.jsonObject },
        { throw VaultAPIException(listOf(VAULT_API_ERROR_NO_BODY)) },
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
    format: Json = VaultClient.json
): List<T> {
    return decodeBodyJsonFieldObject(
        format,
        { it[fieldName]?.jsonArray },
        { throw VaultAPIException(listOf(VAULT_API_ERROR_NO_BODY)) },
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
    format: Json = VaultClient.json
): T? {
    return decodeBodyJsonFieldObject(
        format,
        { it[fieldName]?.jsonObject },
        { null },
        { null }
    )
}

private suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldObject(
    format: Json = VaultClient.json,
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
