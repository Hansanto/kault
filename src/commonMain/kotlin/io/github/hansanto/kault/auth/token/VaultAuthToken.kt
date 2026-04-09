package io.github.hansanto.kault.auth.token

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.VaultAuth
import io.github.hansanto.kault.auth.token.payload.AuthTokenCreateOrUpdateTokenRolePayload
import io.github.hansanto.kault.auth.token.payload.AuthTokenCreateTokenPayload
import io.github.hansanto.kault.auth.token.payload.AuthTokenLookupAccessorTokenPayload
import io.github.hansanto.kault.auth.token.payload.AuthTokenLookupTokenPayload
import io.github.hansanto.kault.auth.token.payload.AuthTokenRenewAccessorTokenPayload
import io.github.hansanto.kault.auth.token.payload.AuthTokenRenewSelfTokenPayload
import io.github.hansanto.kault.auth.token.payload.AuthTokenRenewTokenPayload
import io.github.hansanto.kault.auth.token.payload.AuthTokenRevokeAccessorTokenPayload
import io.github.hansanto.kault.auth.token.payload.AuthTokenRevokeTokenAndOrphanChildrenPayload
import io.github.hansanto.kault.auth.token.payload.AuthTokenRevokeTokenPayload
import io.github.hansanto.kault.auth.token.response.AuthTokenCreateTokenResponse
import io.github.hansanto.kault.auth.token.response.AuthTokenLookupAccessorTokenResponse
import io.github.hansanto.kault.auth.token.response.AuthTokenLookupSelfTokenResponse
import io.github.hansanto.kault.auth.token.response.AuthTokenLookupTokenResponse
import io.github.hansanto.kault.auth.token.response.AuthTokenReadTokenRoleResponse
import io.github.hansanto.kault.auth.token.response.AuthTokenRenewAccessorTokenResponse
import io.github.hansanto.kault.auth.token.response.AuthTokenRenewSelfTokenResponse
import io.github.hansanto.kault.auth.token.response.AuthTokenRenewTokenResponse
import io.github.hansanto.kault.extension.decodeBodyJsonAuthFieldObject
import io.github.hansanto.kault.extension.decodeBodyJsonDataFieldObject
import io.github.hansanto.kault.extension.decodeBodyJsonWarningFieldArray
import io.github.hansanto.kault.extension.list
import io.github.hansanto.kault.response.StandardListResponse
import io.github.hansanto.kault.serializer.VaultDuration
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @see VaultAuthToken.createToken(payload)
 */
public suspend inline fun VaultAuthToken.createToken(
    payloadBuilder: BuilderDsl<AuthTokenCreateTokenPayload>
): AuthTokenCreateTokenResponse {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = AuthTokenCreateTokenPayload().apply(payloadBuilder)
    return createToken(payload)
}

/**
 * @see VaultAuthToken.createToken(roleName, payload)
 */
public suspend inline fun VaultAuthToken.createToken(
    roleName: String,
    payloadBuilder: BuilderDsl<AuthTokenCreateTokenPayload>
): AuthTokenCreateTokenResponse {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = AuthTokenCreateTokenPayload().apply(payloadBuilder)
    return createToken(roleName, payload)
}

/**
 * @see VaultAuthToken.createOrUpdateTokenRole(roleName, payload)
 */
public suspend inline fun VaultAuthToken.createOrUpdateTokenRole(
    roleName: String,
    payloadBuilder: BuilderDsl<AuthTokenCreateOrUpdateTokenRolePayload>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = AuthTokenCreateOrUpdateTokenRolePayload().apply(payloadBuilder)
    return createOrUpdateTokenRole(roleName, payload)
}

/**
 * @see VaultAuthToken.renewToken(payload)
 */
public suspend inline fun VaultAuthToken.renewToken(
    payloadBuilder: BuilderDsl<AuthTokenRenewTokenPayload.Builder>
): AuthTokenRenewTokenResponse {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = AuthTokenRenewTokenPayload.Builder().apply(payloadBuilder).build()
    return renewToken(payload)
}

/**
 * @see VaultAuthToken.renewAccessorToken(payload)
 */
public suspend inline fun VaultAuthToken.renewAccessorToken(
    payloadBuilder: BuilderDsl<AuthTokenRenewAccessorTokenPayload.Builder>
): AuthTokenRenewAccessorTokenResponse {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = AuthTokenRenewAccessorTokenPayload.Builder().apply(payloadBuilder).build()
    return renewAccessorToken(payload)
}

public interface VaultAuthToken {

    /**
     * This endpoint lists token accessor. This requires sudo capability, and access to it should be tightly controlled as the accessors can be used to revoke very large numbers of tokens and their associated leases at once.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#list-accessors)
     * @return List of token accessors.
     */
    public suspend fun listAccessors(): List<String>

    /**
     * Creates a new token.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#create-token)
     * @param payload Token configuration.
     * @return Response.
     */
    public suspend fun createToken(
        payload: AuthTokenCreateTokenPayload = AuthTokenCreateTokenPayload()
    ): AuthTokenCreateTokenResponse

    /**
     * Creates a new token with the specified role name.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#create-token)
     * @param roleName The role name to associate with the token.
     * @param payload Token configuration.
     * @return Response.
     */
    public suspend fun createToken(
        roleName: String,
        payload: AuthTokenCreateTokenPayload = AuthTokenCreateTokenPayload()
    ): AuthTokenCreateTokenResponse

    /**
     * Returns information about the client token.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#lookup-a-token)
     * @param token The token to lookup.
     * @return Response.
     */
    public suspend fun lookupToken(token: String): AuthTokenLookupTokenResponse

    /**
     * Returns information about the current client token.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#lookup-a-token-self)
     * @return Response.
     */
    public suspend fun lookupSelfToken(): AuthTokenLookupSelfTokenResponse

    /**
     * Returns information about the client token from the accessor.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#lookup-a-token-accessor)
     * @param accessor Token accessor to lookup.
     * @return Response.
     */
    public suspend fun lookupAccessorToken(accessor: String): AuthTokenLookupAccessorTokenResponse

    /**
     * Renews a lease associated with a token.
     * This is used to prevent the expiration of a token, and the automatic revocation of it.
     * Token renewal is possible only if there is a lease associated with it.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#renew-a-token)
     * @param payload Token renewal configuration.
     * @return Response.
     */
    public suspend fun renewToken(payload: AuthTokenRenewTokenPayload): AuthTokenRenewTokenResponse

    /**
     * Renews a lease associated with the calling token.
     * This is used to prevent the expiration of a token, and the automatic revocation of it.
     * Token renewal is possible only if there is a lease associated with it.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#renew-a-token-self)
     * @param increment An optional requested increment duration can be provided.
     * This increment may not be honored, for instance in the case of periodic tokens.
     * If not supplied, Vault will use the default TTL.
     * @return Response.
     */
    public suspend fun renewSelfToken(increment: VaultDuration? = null): AuthTokenRenewSelfTokenResponse

    /**
     * Renews a lease associated with a token using its accessor.
     * This is used to prevent the expiration of a token, and the automatic revocation of it.
     * Token renewal is possible only if there is a lease associated with it.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#renew-a-token-accessor)
     * @param payload Token renewal configuration.
     * @return Response.
     */
    public suspend fun renewAccessorToken(
        payload: AuthTokenRenewAccessorTokenPayload
    ): AuthTokenRenewAccessorTokenResponse

    /**
     * Revokes a token and all child tokens.
     * When the token is revoked, all dynamic secrets generated with it are also revoked.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#revoke-a-token)
     * @param token Token to revoke.
     * @return `true` if the token was revoked successfully, `false` otherwise.
     */
    public suspend fun revokeToken(token: String): Boolean

    /**
     * Revokes the token used to call it and all child tokens.
     * When the token is revoked, all dynamic secrets generated with it are also revoked.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#revoke-a-token-self)
     * @return `true` if the token was revoked successfully, `false` otherwise.
     */
    public suspend fun revokeSelfToken(): Boolean

    /**
     * Revoke the token associated with the accessor and all the child tokens.
     * This is meant for purposes where there is no access to token ID
     * but there is need to revoke a token and its children.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#revoke-a-token-accessor)
     * @param accessor Accessor of the token.
     * @return `true` if the token was revoked successfully, `false` otherwise.
     */
    public suspend fun revokeAccessorToken(accessor: String): Boolean

    /**
     * Revokes a token but not its child tokens.
     * When the token is revoked, all secrets generated with it are also revoked.
     * All child tokens are orphaned, but can be revoked sub-sequently using /auth/token/revoke/.
     * This is a root-protected endpoint.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#revoke-token-and-orphan-children)
     * @param token Token to revoke. This can be part of the URL or the body.
     * @return `true` if the token was revoked successfully, `false` otherwise.
     */
    public suspend fun revokeTokenAndOrphanChildren(token: String): Boolean

    /**
     * Fetches the named role configuration.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#read-token-role)
     * @param roleName The name of the token role.
     * @return Response.
     */
    public suspend fun readTokenRole(roleName: String): AuthTokenReadTokenRoleResponse

    /**
     * List available token roles.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#list-token-roles)
     * @return List of token roles.
     */
    public suspend fun listTokenRoles(): List<String>

    /**
     * Creates (or replaces) the named role.
     * Roles enforce specific behavior
     * when creating tokens that allow token functionality
     * that is otherwise not available or would require sudo/root privileges to access.
     * Role parameters, when set, override any provided options to the create endpoints.
     * The role name is also included in the token path,
     * allowing all tokens created against a role to be revoked using the /sys/leases/revoke-prefix endpoint.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#create-update-token-role)
     * @param roleName The name of the token role.
     * @param payload Token role configuration.
     * @return `true` if the role was created or updated successfully, `false` otherwise.
     */
    public suspend fun createOrUpdateTokenRole(
        roleName: String,
        payload: AuthTokenCreateOrUpdateTokenRolePayload = AuthTokenCreateOrUpdateTokenRolePayload()
    ): Boolean

    /**
     * This endpoint deletes the named token role.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#delete-token-role)
     * @param roleName The name of the token role.
     * @return `true` if the role was deleted successfully, `false` otherwise.
     */
    public suspend fun deleteTokenRole(roleName: String): Boolean

    /**
     * Performs some maintenance tasks to clean up invalid entries that may remain in the token store. On Enterprise, Tidy will only impact the tokens in the specified namespace, or the root namespace if unspecified.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#tidy-tokens)
     * @return List of messages due to the tidy operation.
     */
    public suspend fun tidyTokens(): List<String>
}

/**
 * Implementation of [VaultAuthToken].
 */
public class VaultAuthTokenImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultAuthToken {

    public companion object {

        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultAuthTokenImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "token"
    }

    /**
     * Builder class to simplify the creation of [VaultAuth].
     */
    public open class Builder : ServiceBuilder<VaultAuthTokenImpl>() {

        public override var path: String = Default.PATH

        override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultAuthTokenImpl =
            VaultAuthTokenImpl(
                client = client,
                path = completePath
            )
    }

    override suspend fun listAccessors(): List<String> {
        val response = client.list {
            url {
                appendPathSegments(path, "accessors")
            }
        }
        return response.decodeBodyJsonDataFieldObject<StandardListResponse>().keys
    }

    override suspend fun createToken(payload: AuthTokenCreateTokenPayload): AuthTokenCreateTokenResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "create")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.decodeBodyJsonAuthFieldObject()
    }

    override suspend fun createToken(
        roleName: String,
        payload: AuthTokenCreateTokenPayload
    ): AuthTokenCreateTokenResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "create", roleName)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.decodeBodyJsonAuthFieldObject()
    }

    override suspend fun lookupToken(token: String): AuthTokenLookupTokenResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "lookup")
            }
            contentType(ContentType.Application.Json)
            setBody(AuthTokenLookupTokenPayload(token))
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun lookupSelfToken(): AuthTokenLookupSelfTokenResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "lookup-self")
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun lookupAccessorToken(accessor: String): AuthTokenLookupAccessorTokenResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "lookup-accessor")
            }
            contentType(ContentType.Application.Json)
            setBody(AuthTokenLookupAccessorTokenPayload(accessor))
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun renewToken(payload: AuthTokenRenewTokenPayload): AuthTokenRenewTokenResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "renew")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.decodeBodyJsonAuthFieldObject()
    }

    override suspend fun renewSelfToken(increment: VaultDuration?): AuthTokenRenewSelfTokenResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "renew-self")
            }
            contentType(ContentType.Application.Json)
            setBody(AuthTokenRenewSelfTokenPayload(increment))
        }
        return response.decodeBodyJsonAuthFieldObject()
    }

    override suspend fun renewAccessorToken(
        payload: AuthTokenRenewAccessorTokenPayload
    ): AuthTokenRenewAccessorTokenResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "renew-accessor")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.decodeBodyJsonAuthFieldObject()
    }

    override suspend fun revokeToken(token: String): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "revoke")
            }
            contentType(ContentType.Application.Json)
            setBody(AuthTokenRevokeTokenPayload(token))
        }
        return response.status.isSuccess()
    }

    override suspend fun revokeSelfToken(): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "revoke-self")
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun revokeAccessorToken(accessor: String): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "revoke-accessor")
            }
            contentType(ContentType.Application.Json)
            setBody(AuthTokenRevokeAccessorTokenPayload(accessor))
        }
        return response.status.isSuccess()
    }

    override suspend fun revokeTokenAndOrphanChildren(token: String): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "revoke-orphan")
            }
            contentType(ContentType.Application.Json)
            setBody(AuthTokenRevokeTokenAndOrphanChildrenPayload(token))
        }
        return response.status.isSuccess()
    }

    override suspend fun readTokenRole(roleName: String): AuthTokenReadTokenRoleResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "roles", roleName)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun listTokenRoles(): List<String> {
        val response = client.list {
            url {
                appendPathSegments(path, "roles")
            }
        }
        return response.decodeBodyJsonDataFieldObject<StandardListResponse>().keys
    }

    override suspend fun createOrUpdateTokenRole(
        roleName: String,
        payload: AuthTokenCreateOrUpdateTokenRolePayload
    ): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "roles", roleName)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun deleteTokenRole(roleName: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(path, "roles", roleName)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun tidyTokens(): List<String> {
        val response = client.post {
            url {
                appendPathSegments(path, "tidy")
            }
        }
        return response.decodeBodyJsonWarningFieldArray()
    }
}
