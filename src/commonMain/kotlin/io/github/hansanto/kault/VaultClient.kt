package io.github.hansanto.kault

import io.github.hansanto.kault.auth.VaultAuth
import io.github.hansanto.kault.engine.VaultSecretEngine
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.extension.URL_PATH_SEPARATOR
import io.github.hansanto.kault.extension.addURLChildPath
import io.github.hansanto.kault.extension.findErrorFromVaultResponseBody
import io.github.hansanto.kault.system.VaultSystem
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import io.ktor.utils.io.core.Closeable
import kotlinx.coroutines.CoroutineScope
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject

/**
 * Function that resolves the authentication token.
 */
public typealias TokenResolver = () -> String?

/**
 * Function that resolves the namespace.
 */
public typealias NamespaceResolver = () -> String?

/**
 * Client to interact with a Vault server.
 * @property client Http client to interact through REST API.
 * @property namespace Namespace to use.
 * @property auth Authentication service.
 * @property system System service.
 */
public class VaultClient(
    public val client: HttpClient,
    public var namespace: String? = null,
    public val auth: VaultAuth,
    public val system: VaultSystem,
    public val secret: VaultSecretEngine
) : CoroutineScope by client, Closeable by client {

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
        public inline operator fun invoke(builder: BuilderDsl<Builder>): VaultClient =
            Builder().apply(builder).build()
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "v1"

        /**
         * Default headers.
         */
        public val headers: Headers = Headers()
    }

    /**
     * Builder class to simplify the creation of [VaultClient].
     */
    @KaultDsl
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
        private var headerBuilder: BuilderDsl<Headers.Builder> = {}

        /**
         * Builder to define authentication service.
         */
        private var authBuilder: BuilderDsl<VaultAuth.Builder> = {}

        /**
         * Builder to define system service.
         */
        private var sysBuilder: BuilderDsl<VaultSystem.Builder> = {}

        /**
         * Builder to define secret engine service.
         */
        private var secretBuilder: BuilderDsl<VaultSecretEngine.Builder> = {}

        /**
         * Builder to custom the HTTP client.
         * The token resolver is passed as parameter and must not be used before the client is built.
         * [Documentation](https://ktor.io/docs/clients-index.html)
         */
        private var httpClientBuilder: ((TokenResolver, NamespaceResolver) -> HttpClient)? = null

        /**
         * Build the instance of [VaultClient] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): VaultClient {
            lateinit var vaultClient: VaultClient
            val tokenResolver = { vaultClient.auth.token }
            val namespaceResolver = { vaultClient.namespace }
            val client = httpClientBuilder?.invoke(tokenResolver, namespaceResolver) ?: createHttpClient(
                tokenResolver,
                namespaceResolver
            )

            return VaultClient(
                client = client,
                namespace = this.namespace,
                auth = VaultAuth(client, null, this.authBuilder),
                system = VaultSystem(client, null, this.sysBuilder),
                secret = VaultSecretEngine(client, this.secretBuilder)
            ).also { vaultClient = it }
        }

        /**
         * Sets the header builder.
         *
         * @param builder Builder to create [Headers] instance.
         */
        public fun headers(builder: BuilderDsl<Headers.Builder>) {
            headerBuilder = builder
        }

        /**
         * Sets the authentication service builder.
         *
         * @param builder Builder to create [VaultAuth] instance.
         */
        public fun auth(builder: BuilderDsl<VaultAuth.Builder>) {
            authBuilder = builder
        }

        /**
         * Sets the system service builder.
         *
         * @param builder Builder to create [VaultSystem] instance.
         */
        public fun system(builder: BuilderDsl<VaultSystem.Builder>) {
            sysBuilder = builder
        }

        /**
         * Sets the secret engine service builder.
         *
         * @param builder Builder to create [VaultSecretEngine] instance.
         */
        public fun secret(builder: BuilderDsl<VaultSecretEngine.Builder>) {
            secretBuilder = builder
        }

        /**
         * Sets the HTTP client builder.
         *
         * @param builder Builder to create [HttpClientConfig] instance.
         */
        public fun httpClient(builder: ((TokenResolver, NamespaceResolver) -> HttpClient)?) {
            httpClientBuilder = builder
        }

        /**
         * Creates an HttpClient with the default configuration.
         *
         * @param tokenResolver Function that resolves the authentication token.
         * @param namespaceResolver Function that returns the namespace.
         * @return The configured [HttpClient] instance.
         */
        private fun createHttpClient(tokenResolver: TokenResolver, namespaceResolver: NamespaceResolver): HttpClient =
            HttpClient {
                defaultHttpClientConfiguration(tokenResolver, namespaceResolver)
            }

        /**
         * Configures the default HttpClient settings to interact with the Vault API.
         *
         * @param tokenResolver Function that returns the token for authentication.
         * @return Function which can be used to configure the HttpClient.
         */
        public fun HttpClientConfig<*>.defaultHttpClientConfiguration(
            tokenResolver: TokenResolver,
            namespaceResolver: NamespaceResolver
        ) {
            install(ContentNegotiation) {
                json(json)
            }

            HttpResponseValidator {
                validateResponse { response ->
                    if (!response.status.isSuccess()) {
                        val text = response.bodyAsText()
                        if (text.isEmpty()) {
                            return@validateResponse
                        }

                        val jsonBody = json.parseToJsonElement(text).jsonObject
                        val errors = findErrorFromVaultResponseBody(jsonBody)
                        if (errors != null) {
                            throw VaultAPIException(errors)
                        }
                    }
                }
            }

            val headers = Headers(headerBuilder)
            val baseUrl = this@Builder.url.addURLChildPath(path) + URL_PATH_SEPARATOR
            defaultRequest {
                url(baseUrl)
                header(headers.token, tokenResolver())
                header(headers.namespace, namespaceResolver())
            }
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
            public inline operator fun invoke(builder: BuilderDsl<Builder>): Headers =
                Builder().apply(builder).build()
        }

        /**
         * Builder class to simplify the creation of [Headers].
         */
        @KaultDsl
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
}
