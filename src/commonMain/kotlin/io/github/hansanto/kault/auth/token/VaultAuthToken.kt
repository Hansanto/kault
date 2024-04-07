package io.github.hansanto.kault.auth.token

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.VaultAuth
import io.github.hansanto.kault.auth.token.payload.TokenCreatePayload
import io.github.hansanto.kault.serializer.VaultDuration
import io.ktor.client.HttpClient
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @see VaultAuthToken.createToken(payload)
 */
public suspend inline fun VaultAuthToken.createOrUpdate(
    payloadBuilder: BuilderDsl<TokenCreatePayload>
): Any {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = TokenCreatePayload().apply(payloadBuilder)
    return createToken(payload)
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
     * @return Any
     */
    public suspend fun createToken(payload: TokenCreatePayload): Any

    /**
     * Returns information about the client token.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#lookup-a-token)
     * @param token The token to lookup.
     * @return Any
     */
    public suspend fun lookupToken(token: String): Any

    /**
     * Returns information about the current client token.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#lookup-a-token-self)
     * @return Any
     */
    public suspend fun lookupSelfToken(): Any

    /**
     * Returns information about the client token from the accessor.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#lookup-a-token-accessor)
     * @param accessor Token accessor to lookup.
     * @return Any
     */
    public suspend fun lookupAccessorToken(accessor: String): Any

    /**
     * Renews a lease associated with a token.
     * This is used to prevent the expiration of a token, and the automatic revocation of it.
     * Token renewal is possible only if there is a lease associated with it.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#renew-a-token)
     * @param payload Any
     * @return Any
     */
    public suspend fun renewToken(payload: Any): Any

    /**
     * Renews a lease associated with the calling token.
     * This is used to prevent the expiration of a token, and the automatic revocation of it.
     * Token renewal is possible only if there is a lease associated with it.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#renew-a-token-self)
     * @param increment An optional requested increment duration can be provided.
     * This increment may not be honored, for instance in the case of periodic tokens.
     * If not supplied, Vault will use the default TTL.
     * @return Any
     */
    public suspend fun renewSelfToken(increment: VaultDuration): Any

    /**
     * Renews a lease associated with a token using its accessor.
     * This is used to prevent the expiration of a token, and the automatic revocation of it.
     * Token renewal is possible only if there is a lease associated with it.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#renew-a-token-accessor)
     * @param payload Any
     * @return Any
     */
    public suspend fun renewAccessorToken(payload: Any): Any

    /**
     * Revokes a token and all child tokens.
     * When the token is revoked, all dynamic secrets generated with it are also revoked.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#revoke-a-token)
     * @param token Token to revoke.
     * @return Any
     */
    public suspend fun revokeToken(token: String): Any

    /**
     * Revokes the token used to call it and all child tokens.
     * When the token is revoked, all dynamic secrets generated with it are also revoked.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#revoke-a-token-self)
     * @return Any
     */
    public suspend fun revokeSelfToken(): Any

    /**
     * Revoke the token associated with the accessor and all the child tokens.
     * This is meant for purposes where there is no access to token ID
     * but there is need to revoke a token and its children.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#revoke-a-token-accessor)
     * @param accessor Accessor of the token.
     * @return Any
     */
    public suspend fun revokeAccessorToken(accessor: String): Any

    /**
     * Revokes a token but not its child tokens.
     * When the token is revoked, all secrets generated with it are also revoked.
     * All child tokens are orphaned, but can be revoked sub-sequently using /auth/token/revoke/.
     * This is a root-protected endpoint.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#revoke-token-and-orphan-children)
     * @param token Token to revoke. This can be part of the URL or the body.
     * @return Any
     */
    public suspend fun revokeTokenAndOrphan(token: String): Any

    /**
     * Fetches the named role configuration.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#read-token-role)
     * @param roleName The name of the token role.
     * @return Any
     */
    public suspend fun readTokenRole(roleName: String): Any

    /**
     * List available token roles.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#list-token-roles)
     * @return Any
     */
    public suspend fun listTokenRoles(): Any

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
     * @param payload Any
     * @return Any
     */
    public suspend fun createOrUpdateTokenRole(roleName: String, payload: Any): Any

    /**
     * This endpoint deletes the named token role.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#delete-token-role)
     * @param roleName The name of the token role.
     * @return Any
     */
    public suspend fun deleteTokenRole(roleName: String): Any

    /**
     * Performs some maintenance tasks to clean up invalid entries that may remain in the token store. On Enterprise, Tidy will only impact the tokens in the specified namespace, or the root namespace if unspecified.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/token#tidy-tokens)
     * @return Any
     */
    public suspend fun tidyTokens(): Any
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
    public class Builder : ServiceBuilder<VaultAuthTokenImpl>() {

        public override var path: String = Default.PATH

        override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultAuthTokenImpl {
            return VaultAuthTokenImpl(
                client = client,
                path = completePath
            )
        }
    }

}
