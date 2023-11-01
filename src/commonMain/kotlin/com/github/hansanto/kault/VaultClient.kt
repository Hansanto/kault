package com.github.hansanto.kault

import com.github.hansanto.kault.auth.VaultAuth
import com.github.hansanto.kault.exception.VaultErrorResponse
import com.github.hansanto.kault.exception.VaultException
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import io.ktor.http.takeFrom
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Client for interacting with HashiCorp Vault server.
 */
public class VaultClient(
    url: String,
    namespace: String? = null,
    apiPath: String = Default.apiPath,
    headers: Headers = Headers()
) {

    public companion object {

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

        public val headers: Headers = Headers()

        public val apiPath: String = "/v1/"

    }

    /**
     * Builder class to simplify the creation of [VaultClient].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder {

        public lateinit var url: String

        public var namespace: String? = null

        public var apiPath: String = Default.apiPath

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

    public data class Headers(
        public val token: String = "X-Vault-Token",
        public val namespace: String = "X-Vault-Namespace"
    ) {
        public companion object {
            public inline operator fun invoke(builder: Builder.() -> Unit): Headers =
                Builder().apply(builder).build()
        }

        @Suppress("MemberVisibilityCanBePrivate")
        public class Builder {

            public var token: String = Default.headers.token

            public var namespace: String = Default.headers.namespace

            public fun build(): Headers = Headers(
                token = token,
                namespace = namespace
            )
        }
    }

    private val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    explicitNulls = false
                    ignoreUnknownKeys = true
                }
            )
        }

        HttpResponseValidator {
            validateResponse { response ->
                if (!response.status.isSuccess()) {
                    val error = response.body<VaultErrorResponse>()
                    throw VaultException(error.errors)
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
