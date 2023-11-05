package com.github.hansanto.kault

import com.github.hansanto.kault.auth.VaultAuth
import com.github.hansanto.kault.exception.VaultAPIException
import com.github.hansanto.kault.system.VaultSystem
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

/**
 * Client to interact with a Vault server.
 * @property client Http client to interact through REST API.
 * @property auth Authentication service.
 */
public class VaultClient(
    /**
     * @see [VaultClient.Builder.url]
     */
    url: String,

    /**
     * @see [VaultClient.Builder.namespace]
     */
    namespace: String? = null,

    /**
     * @see [VaultClient.Builder.path]
     */
    apiPath: String = Default.PATH,

    /**
     * Headers to use for the requests.
     */
    headers: Headers = Default.headers,

    /**
     * Builder to define authentication service.
     */
    authBuilder: VaultAuth.Builder.() -> Unit = Default.authBuilder,

    ) {

    public companion object {

        /**
         * JSON configuration to parse payloads and responses.
         */
        public val json: Json = Json {
            explicitNulls = false
            isLenient = true
            ignoreUnknownKeys = true
        }

        /**
         * Create a new instance of [VaultClient] using the builder pattern.
         * ```kotlin
         * val client = VaultClient {
         *   url = "http://localhost:8200"
         *   // ...
         * }
         * ```
         * @param builder Builder to create the instance.
         * @return Instance of [VaultClient].
         */
        public inline operator fun invoke(builder: Builder.() -> Unit): VaultClient =
            Builder().apply(builder).build()
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "/v1/"

        /**
         * Default headers.
         */
        public val headers: Headers = Headers()

        /**
         * Default authentication service builder.
         */
        public val authBuilder: VaultAuth.Builder.() -> Unit = {}
    }

    /**
     * Builder class to simplify the creation of [VaultClient].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder {

        /**
         * URL of the Vault server.
         * Example: `http://localhost:8200`
         */
        public lateinit var url: String

        /**
         * Namespace to use.
         * [Documentation](https://developer.hashicorp.com/vault/docs/enterprise/namespaces)
         */
        public var namespace: String? = null

        /**
         * Path to the API.
         * [Documentation](https://developer.hashicorp.com/vault/api-docs)
         */
        public var path: String = Default.PATH

        /**
         * Builder to define header keys.
         */
        private var headers: Headers.Builder.() -> Unit = {}

        /**
         * Builder to define authentication service.
         */
        private var auth: VaultAuth.Builder.() -> Unit = Default.authBuilder

        /**
         * Build the instance of [VaultClient] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): VaultClient = VaultClient(
            url = url,
            namespace = namespace,
            apiPath = path,
            headers = Headers(headers),
            authBuilder = auth
        )

        /**
         * Sets the header builder.
         *
         * @param builder Builder to create [Headers] instance.
         */
        public fun headers(builder: Headers.Builder.() -> Unit) {
            headers = builder
        }

        /**
         * Sets the authentication service builder.
         *
         * @param builder Builder to create [VaultAuth] instance.
         */
        public fun auth(builder: VaultAuth.Builder.() -> Unit) {
            auth = builder
        }
    }

    /**
     * Represents the headers used for interacting with a server.
     *
     * @property token The header name for the authentication token.
     * @property namespace The header name for the namespace.
     */
    public data class Headers(
        public val token: String = "X-Vault-Token",
        public val namespace: String = "X-Vault-Namespace"
    ) {

        public companion object {

            /**
             * Create a new instance of [Headers] using the builder pattern.
             * @param builder Builder to create the instance.
             * @return Instance of [Headers].
             */
            public inline operator fun invoke(builder: Builder.() -> Unit): Headers =
                Builder().apply(builder).build()
        }

        /**
         * Builder class to simplify the creation of [Headers].
         */
        @Suppress("MemberVisibilityCanBePrivate")
        public class Builder {

            /**
             * Header to define the token to interact with the server.
             */
            public var token: String = Default.headers.token

            /**
             * Header to define the namespace.
             */
            public var namespace: String = Default.headers.namespace

            /**
             * Build the instance of [Headers] with the values defined in builder.
             * @return Instance of [Headers].
             */
            public fun build(): Headers = Headers(
                token = token,
                namespace = namespace
            )
        }
    }

    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(json)
        }

        install(Logging) {
            logger = Logger.DEFAULT
            level = LogLevel.ALL
            sanitizeHeader {
                it == headers.token
            }
        }

        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    throw VaultAPIException(findErrorsInResponse(response))
                }
            }
        }

        defaultRequest {
            url {
                takeFrom(url)
                appendPathSegments(apiPath)
            }

            header(headers.token, auth.token)
            header(headers.namespace, namespace)
        }
    }

    public val auth: VaultAuth = VaultAuth(client, authBuilder)

    public val system: VaultSystem = VaultSystem(client)

    /**
     * Finds errors in the given HttpResponse.
     * When checking fields,
     * we force the type of json element to know if the format changes between several versions of the API.
     *
     * @param response The HttpResponse to check for errors.
     * @return A list of error messages found in the response. Returns an empty list if no errors are found.
     */
    private suspend fun findErrorsInResponse(response: HttpResponse): List<String> {
        if (response.contentType() != null) {
            val jsonBody = json.parseToJsonElement(response.bodyAsText()).jsonObject
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
        }
        return emptyList()
    }
}
