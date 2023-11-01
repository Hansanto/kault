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
    namespace: String? = null
) {

    public companion object {
        public const val PREFIX_PATH: String = "/v1/"
        public const val TOKEN_HEADER: String = "X-Vault-Token"
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
                if(!response.status.isSuccess()) {
                    val error = response.body<VaultErrorResponse>()
                    throw VaultException(error.errors)
                }
            }
        }

        defaultRequest {
            url {
                takeFrom(url)
                appendPathSegments(PREFIX_PATH)
                namespace?.let {
                    appendPathSegments("/$it/")
                }
            }

            header(TOKEN_HEADER, auth.token)
        }
    }

    public val auth: VaultAuth = VaultAuth(client)
}
