package io.github.hansanto.kault.auth.token

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.VaultAuth
import io.ktor.client.HttpClient

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
    public suspend fun createToken(payload: Any): Any

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
