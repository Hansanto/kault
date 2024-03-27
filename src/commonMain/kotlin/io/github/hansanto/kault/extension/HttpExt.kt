package io.github.hansanto.kault.extension

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.exception.VaultFieldNotFoundException
import io.github.hansanto.kault.response.ResponseFields
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
 * Represents the separator used in URLs.
 *
 * This constant defines the string value "/" which is commonly used as a separator in URLs.
 */
public const val URL_PATH_SEPARATOR: String = "/"

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
 * Decodes the response body as a JSON object and returns the value of the "warnings" field.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @return Decoded value of the specified field.
 */
public suspend inline fun HttpResponse.decodeBodyJsonWarningFieldArray(format: Json = VaultClient.json): List<String> {
    return decodeBodyJsonFieldArray(ResponseFields.WARNINGS, format)
}

/**
 * Decodes the response body as a JSON object and returns the value of the "auth" field.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @return Decoded value of the specified field.
 */
public suspend inline fun <reified T> HttpResponse.decodeBodyJsonAuthFieldObject(format: Json = VaultClient.json): T {
    return decodeBodyJsonFieldObject(ResponseFields.AUTH, format)
}

/**
 * Decodes the response body as a JSON object and returns the value of the "data" field.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @return Decoded value of the specified field.
 */
public suspend inline fun <reified T> HttpResponse.decodeBodyJsonDataFieldObject(format: Json = VaultClient.json): T {
    return decodeBodyJsonFieldObject(ResponseFields.DATA, format)
}

/**
 * Decodes the response body as a JSON object and returns the value of the "data" field.
 * If the body is null or the field is not found, null is returned.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @return Decoded value of the specified field.
 */
public suspend inline fun <reified T> HttpResponse.decodeBodyJsonDataFieldObjectOrNull(
    format: Json = VaultClient.json
): T? {
    return decodeBodyJsonFieldObjectOrNull(ResponseFields.DATA, format)
}

/**
 * Decodes the response body as a JSON object and returns the value of the specified field.
 * If the body is null or the field is not found, an exception is thrown.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @return Decoded value from the specified field.
 */
public suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldObject(
    fieldName: String,
    format: Json = VaultClient.json
): T {
    return decodeBodyJsonFieldObject(
        { it[fieldName]?.jsonObject },
        { throw VaultAPIException() },
        format
    ) ?: throw VaultFieldNotFoundException(fieldName)
}

/**
 * Decodes the response body as a JSON object and returns the value of the specified field.
 * If the body is null or the field is not found, null is returned.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param format Format to use to decode the JSON object.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @return Decoded value from the specified field.
 */
public suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldObjectOrNull(
    fieldName: String,
    format: Json = VaultClient.json
): T? {
    return decodeBodyJsonFieldObject(
        { it[fieldName]?.jsonObject },
        { null },
        format
    )
}

/**
 * Decodes the response body as a JSON array and returns the value of the specified field as a list.
 * If the body is null or the field is not found, an exception is thrown.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param fieldName Name of the JSON field to retrieve the value from.
 * @param format Format to use to decode the JSON object.
 * @return List of decoded values from the specified field.
 */
public suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldArray(
    fieldName: String,
    format: Json = VaultClient.json
): List<T> {
    return decodeBodyJsonFieldObject(
        { it[fieldName]?.jsonArray },
        { throw VaultAPIException() },
        format
    ) ?: throw VaultFieldNotFoundException(fieldName)
}

/**
 * Decodes the response body as a JSON object and returns the value of the specified field.
 *
 * @receiver HttpResponse the HTTP response that contains the body to extract the JSON field from.
 * @param getJsonField Lambda that returns the JSON field to extract the value from, or null if the field is not found.
 * @param onEmptyBody Value to return when the response body is empty.
 * @param format Format to use to decode the JSON object.
 * @return `null` if the field returned by [getJsonField] is `null`, the decoded value otherwise.
 */
public suspend inline fun <reified T> HttpResponse.decodeBodyJsonFieldObject(
    getJsonField: (JsonObject) -> JsonElement?,
    onEmptyBody: () -> T,
    format: Json = VaultClient.json
): T? {
    val text = bodyAsText()
    if (text.isEmpty()) {
        return onEmptyBody()
    }

    val jsonBody = format.parseToJsonElement(text).jsonObject
    return getJsonField(jsonBody)?.let(format::decodeFromJsonElement)
}

/**
 * Find an error message from Vault response body.
 * If the field "errors" or "data.error" is found, returns the list of errors, otherwise null.
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
