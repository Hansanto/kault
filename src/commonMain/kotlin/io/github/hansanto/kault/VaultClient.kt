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
 * Headers used in each request to the [VaultClient].
 */
public typealias Headers = Map<String, String?>

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
         * Default headers builder.
         */
        public val headers: BuilderDslWithArg<MutableMap<String, String?>, VaultClient> = { client ->
            put("X-Vault-Token", client.auth.getToken())
            put("X-Vault-Namespace", client.namespace)
        }
    }

    /**
     * Builder class to simplify the creation of [VaultClient].
     */
    @KaultDsl
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
         * Builder to define headers to use in each request.
         */
        private var headerBuilder: BuilderDslWithArg<MutableMap<String, String?>, VaultClient> = Default.headers

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
        private var httpClientBuilder: ((() -> Headers) -> HttpClient)? = null

        /**
         * Build the instance of [VaultClient] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): VaultClient {
            lateinit var vaultClient: VaultClient
            val headerBuilder: () -> Headers = {
                buildMap { headerBuilder(vaultClient) }
            }
            val client = httpClientBuilder?.invoke(headerBuilder) ?: createHttpClient(headerBuilder)

            return VaultClient(
                client = client,
                namespace = this.namespace,
                auth = VaultAuth(client, null, this.authBuilder),
                system = VaultSystem(client, null, this.sysBuilder),
                secret = VaultSecretEngine(client, null, this.secretBuilder)
            ).also { vaultClient = it }
        }

        /**
         * Sets the header builder.
         *
         * @param builder Builder to create [Headers] instance.
         */
        public fun headers(builder: BuilderDslWithArg<MutableMap<String, String?>, VaultClient>) {
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
         * @param builder Builder to create [HttpClientConfig] instance with the header builder as parameter.
         */
        public fun httpClient(builder: ((() -> Headers) -> HttpClient)?) {
            httpClientBuilder = builder
        }

        /**
         * Creates an HttpClient with the default configuration.
         *
         * @param headerBuilder Builder to create [Headers] instance.
         * @return The configured [HttpClient] instance.
         */
        private inline fun createHttpClient(crossinline headerBuilder: () -> Headers): HttpClient =
            HttpClient {
                defaultHttpClientConfiguration(headerBuilder)
            }

        /**
         * Configures the default HttpClient settings to interact with the Vault API.
         *
         * @param headerBuilder Builder to create [Headers] instance.
         * @return Function which can be used to configure the HttpClient.
         */
        public inline fun HttpClientConfig<*>.defaultHttpClientConfiguration(crossinline headerBuilder: () -> Headers) {
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

            val baseUrl = this@Builder.url.addURLChildPath(path) + URL_PATH_SEPARATOR
            defaultRequest {
                url(baseUrl)

                headerBuilder().forEach { (key, value) ->
                    header(key, value)
                }
            }
        }
    }
}
