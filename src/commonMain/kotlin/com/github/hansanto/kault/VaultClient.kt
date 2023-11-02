package com.github.hansanto.kault

import com.github.hansanto.kault.auth.VaultAuth
import com.github.hansanto.kault.exception.VaultAPIException
import com.github.hansanto.kault.exception.VaultErrorResponse
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

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
     * @see [VaultClient.Builder.apiPath]
     */
    apiPath: String = Default.apiPath,

    /**
     * Headers to use for the requests.
     */
    headers: Headers = Default.headers
) {

    public companion object {

        internal val json: Json = Json {
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
         * Default headers.
         */
        public val headers: Headers = Headers()

        /**
         * Default API path.
         */
        public val apiPath: String = "/v1/"
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
        public var apiPath: String = Default.apiPath

        /**
         * Builder to define header keys.
         */
        public var headers: Headers.Builder.() -> Unit = {}

        /**
         * Build the instance of [VaultClient] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): VaultClient = VaultClient(
            url = url,
            namespace = namespace,
            apiPath = apiPath,
            headers = Headers(headers)
        )
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
            level = LogLevel.INFO
            sanitizeHeader {
                it == headers.token
            }
        }

        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    val error = response.body<VaultErrorResponse>()
                    throw VaultAPIException(error.errors)
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

    public val auth: VaultAuth = VaultAuth(client)
}
