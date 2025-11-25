package io.github.hansanto.kault.auth.oidc.payload

import io.github.hansanto.kault.KaultDsl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCAuthorizationUrlPayload(
    /**
     * Path to the callback to complete the login. This will be of the form, "https://.../oidc/callback" where the leading portion is dependent on your Vault server location, port, and the mount of the JWT plugin. This must be configured with Vault and the provider. See [Redirect URIs](https://developer.hashicorp.com/vault/docs/auth/jwt#redirect-uris) for more information.
     */
    @SerialName("redirect_uri")
    public var redirectUri: String,

    /**
     * Name of the role against which the login is being attempted. Defaults to configured default_role if not provided.
     */
    @SerialName("role")
    public var role: String? = null,

    /**
     * Optional client-provided nonce that must match the client_nonce value provided during a subsequent request to the [callback](https://developer.hashicorp.com/vault/api-docs/auth/jwt#oidc-callback) API.
     */
    @SerialName("client_nonce")
    public var clientNonce: String? = null,

) {

    /**
     * Builder class to simplify the creation of [OIDCAuthorizationUrlPayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [OIDCAuthorizationUrlPayload.redirectUri]
         */
        public lateinit var redirectUri: String

        /**
         * @see [OIDCAuthorizationUrlPayload.role]
         */
        public var role: String? = null

        /**
         * @see [OIDCAuthorizationUrlPayload.clientNonce]
         */
        public var clientNonce: String? = null

        /**
         * Build the instance of [OIDCAuthorizationUrlPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): OIDCAuthorizationUrlPayload = OIDCAuthorizationUrlPayload(
            redirectUri = redirectUri,
            role = role,
            clientNonce = clientNonce,
        )
    }
}
