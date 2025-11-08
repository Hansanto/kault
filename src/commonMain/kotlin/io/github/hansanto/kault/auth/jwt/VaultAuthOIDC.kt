package io.github.hansanto.kault.auth.jwt

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.common.response.LoginResponse
import io.github.hansanto.kault.auth.jwt.payload.OIDCAuthorizationUrlPayload
import io.github.hansanto.kault.auth.jwt.payload.OIDCCallbackPayload
import io.github.hansanto.kault.auth.jwt.payload.OIDCConfigurePayload
import io.github.hansanto.kault.auth.jwt.payload.OIDCCreateOrUpdatePayload
import io.github.hansanto.kault.auth.jwt.payload.OIDCJwtLoginPayload
import io.github.hansanto.kault.auth.jwt.response.OIDCAuthorizationUrlResponse
import io.github.hansanto.kault.auth.jwt.response.OIDCConfigureResponse
import io.github.hansanto.kault.auth.jwt.response.OIDCReadRoleResponse
import io.github.hansanto.kault.extension.decodeBodyJsonAuthFieldObject
import io.github.hansanto.kault.extension.decodeBodyJsonDataFieldObject
import io.github.hansanto.kault.extension.list
import io.github.hansanto.kault.response.StandardListResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @see VaultAuthOIDC.configure(payload)
 */
public suspend inline fun VaultAuthOIDC.configure(payloadBuilder: BuilderDsl<OIDCConfigurePayload.Builder>): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = OIDCConfigurePayload.Builder().apply(payloadBuilder).build()
    return configure(payload)
}

/**
 * @see VaultAuthOIDC.createOrUpdateRole(roleName, payload)
 */
public suspend inline fun VaultAuthOIDC.createOrUpdateRole(
    roleName: String,
    payloadBuilder: BuilderDsl<OIDCCreateOrUpdatePayload.Builder>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = OIDCCreateOrUpdatePayload.Builder().apply(payloadBuilder).build()
    return createOrUpdateRole(roleName, payload)
}

/**
 * @see VaultAuthOIDC.oidcAuthorizationUrl(payload)
 */
public suspend inline fun VaultAuthOIDC.oidcAuthorizationUrl(
    payloadBuilder: BuilderDsl<OIDCAuthorizationUrlPayload.Builder>
): String {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = OIDCAuthorizationUrlPayload.Builder().apply(payloadBuilder).build()
    return oidcAuthorizationUrl(payload)
}

/**
 * @see VaultAuthOIDC.oidcCallback(parameters)
 */
public suspend inline fun VaultAuthOIDC.oidcCallback(
    parametersBuilder: BuilderDsl<OIDCCallbackPayload.Builder>
): LoginResponse {
    contract { callsInPlace(parametersBuilder, InvocationKind.EXACTLY_ONCE) }
    val parameters = OIDCCallbackPayload.Builder().apply(parametersBuilder).build()
    return oidcCallback(parameters)
}

/**
 * @see VaultAuthOIDC.jwtLogin(payload)
 */
public suspend inline fun VaultAuthOIDC.jwtLogin(
    payloadBuilder: BuilderDsl<OIDCJwtLoginPayload.Builder>
): LoginResponse {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = OIDCJwtLoginPayload.Builder().apply(payloadBuilder).build()
    return jwtLogin(payload)
}

/**
 * Provides methods for managing Kubernetes authentication within Vault.
 */
public interface VaultAuthOIDC {

    /**
     * The JWT authentication backend validates JWTs (or OIDC) using the configured credentials. If using OIDC Discovery, the URL must be provided, along with (optionally) the CA cert to use for the connection. If performing JWT validation locally, a set of public keys must be provided.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/jwt#configure)
     * @return Returns true if the configuration was updated successfully.
     */
    public suspend fun configure(payload: OIDCConfigurePayload): Boolean

    /**
     * Returns the previously configured config.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/jwt#read-config)
     * @return Response.
     */
    public suspend fun readConfiguration(): OIDCConfigureResponse

    /**
     * Registers a role in the method. Role types have specific entities that can perform login operations against this endpoint. Constraints specific to the role type must be set on the role. These are applied to the authenticated entities attempting to login. At least one of the bound values must be set.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/jwt#create-update-role)
     *
     * @param roleName Name of the role.
     * @param payload Payload to create or update the role.
     * @return Returns true if the role was created or updated successfully.
     */
    public suspend fun createOrUpdateRole(roleName: String, payload: OIDCCreateOrUpdatePayload): Boolean

    /**
     * Returns the previously registered role configuration.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/jwt#read-role)
     *
     * @param roleName Name of the role.
     * @return Response.
     */
    public suspend fun readRole(roleName: String): OIDCReadRoleResponse

    /**
     * Lists all the roles that are registered with the plugin.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/jwt#list-roles)
     * @return List of role names.
     */
    public suspend fun list(): List<String>

    /**
     * Deletes the previously registered role.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/jwt#delete-role)
     * @param roleName Name of the role.
     * @return Returns true if the role was deleted successfully.
     */
    public suspend fun deleteRole(roleName: String): Boolean

    /**
     * Obtain an authorization URL from Vault to start an OIDC login flow.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/jwt#oidc-authorization-url-request)
     * @param payload Payload to obtain the authorization URL.
     * @return The OIDC authorization URL to which the client should be redirected.
     */
    public suspend fun oidcAuthorizationUrl(payload: OIDCAuthorizationUrlPayload): String

    /**
     * Exchange an authorization code for an OIDC ID Token. The ID token will be further validated against any bound claims, and if valid a Vault token will be returned.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/jwt#oidc-callback)
     * @param payload Payload for the OIDC callback.
     * @return Response.
     */
    public suspend fun oidcCallback(payload: OIDCCallbackPayload): LoginResponse

    /**
     * Fetch a token. This endpoint takes a signed JSON Web Token (JWT) and a role name for some entity. It verifies the JWT signature to authenticate that entity and then authorizes the entity for the given role.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/jwt#jwt-login)
     * @param payload Parameters to login with JWT.
     * @return Response.
     */
    public suspend fun jwtLogin(payload: OIDCJwtLoginPayload): LoginResponse
}

/**
 * Implementation of [VaultAuthOIDC].
 */
public class VaultAuthOIDCImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultAuthOIDC {

    public companion object {

        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultAuthOIDCImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "oidc"
    }

    /**
     * Builder class to simplify the creation of [VaultAuthOIDCImpl].
     */
    public open class Builder : ServiceBuilder<VaultAuthOIDCImpl>() {

        public override var path: String = Default.PATH

        public override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultAuthOIDCImpl =
            VaultAuthOIDCImpl(
                client = client,
                path = completePath
            )
    }

    override suspend fun configure(payload: OIDCConfigurePayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "config")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun readConfiguration(): OIDCConfigureResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "config")
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun createOrUpdateRole(roleName: String, payload: OIDCCreateOrUpdatePayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "role", roleName)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun readRole(roleName: String): OIDCReadRoleResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "role", roleName)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun list(): List<String> {
        val response = client.list {
            url {
                appendPathSegments(path, "role")
            }
        }
        return response.decodeBodyJsonDataFieldObject<StandardListResponse>().keys
    }

    override suspend fun deleteRole(roleName: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(path, "role", roleName)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun oidcAuthorizationUrl(payload: OIDCAuthorizationUrlPayload): String {
        val response = client.post {
            url {
                appendPathSegments(path, "oidc", "auth_url")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.decodeBodyJsonDataFieldObject<OIDCAuthorizationUrlResponse>().authUrl
    }

    override suspend fun oidcCallback(payload: OIDCCallbackPayload): LoginResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "oidc", "callback")
                parameter("state", payload.state)
                parameter("nonce", payload.nonce)
                parameter("code", payload.code)
                payload.clientNonce?.let { parameter("client_nonce", it) }
            }
        }
        return response.decodeBodyJsonAuthFieldObject()
    }

    override suspend fun jwtLogin(payload: OIDCJwtLoginPayload): LoginResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "login")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.decodeBodyJsonAuthFieldObject()
    }
}
