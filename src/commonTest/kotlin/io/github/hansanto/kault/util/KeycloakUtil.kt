package io.github.hansanto.kault.util

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.forms.submitForm
import io.ktor.http.Parameters
import io.ktor.serialization.kotlinx.json.json
import io.ktor.util.decodeBase64String
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject

data class JwtInfo(val token: String, val subject: String)

object KeycloakUtil {
    const val VAULT_PROVIDER_ID: String = "keycloak"

    const val HOST_FOR_LOCAL: String = "http://localhost:8080"
    const val HOST_FOR_VAULT: String = "http://keycloak:8080"
    const val REALM: String = "vault"
    const val CLIENT_ID: String = "vault"
    const val CLIENT_SECRET: String = "vault-client-secret"
    const val USERNAME: String = "vault-user"
    const val PASSWORD: String = "password"

    private val httpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                }
            )
        }

        this.expectSuccess = true
    }

    suspend fun getJwtWithPayload(
        realm: String = REALM,
        clientId: String = CLIENT_ID,
        clientSecret: String = CLIENT_SECRET,
        username: String = USERNAME,
        password: String = PASSWORD
    ): JwtInfo {
        val jwt = getJwtToken(
            realm = realm,
            clientId = clientId,
            clientSecret = clientSecret,
            username = username,
            password = password
        )
        val jwtPayload = jwt.split(".")[1]
        val payload = Json.decodeFromString<JsonObject>(
            jwtPayload.decodeBase64String()
        )
        val subject = payload["sub"]?.toString()?.replace("\"", "") ?: error("Missing sub claim in jwt")
        return JwtInfo(
            token = jwt,
            subject = subject
        )
    }

    suspend fun getJwtToken(
        realm: String = REALM,
        clientId: String = CLIENT_ID,
        clientSecret: String = CLIENT_SECRET,
        username: String = USERNAME,
        password: String = PASSWORD
    ): String = httpClient.submitForm(
        url = getTokenUrl(realm),
        formParameters = Parameters.build {
            append("grant_type", "password")
            append("client_id", clientId)
            append("client_secret", clientSecret)
            append("username", username)
            append("password", password)
            append("scope", "openid")
        }
    ).body<KeycloakTokenResponse>().accessToken

    fun getJwksUrl(realm: String = REALM): String = "$HOST_FOR_VAULT/realms/$realm/protocol/openid-connect/certs"

    fun getTokenUrl(realm: String = REALM): String = "$HOST_FOR_LOCAL/realms/$realm/protocol/openid-connect/token"
}

@Serializable
data class KeycloakTokenResponse(
    @SerialName("access_token")
    val accessToken: String,
)
