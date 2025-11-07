package io.github.hansanto.kault.auth.jwt.payload

import io.github.hansanto.kault.KaultDsl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCCallbackPayload(
    /**
     * Opaque state ID that is part of the Authorization URL and will be included in the the redirect following successful authentication on the provider.
     */
    @SerialName("state")
    public var state: String,

    /**
     * Provider-generated authorization code that Vault will exchange for an ID token.
     */
    @SerialName("code")
    public var code: String,

    /**
     * Opaque nonce that is part of the Authorization URL and will be included in the the redirect following successful authentication on the provider.
     */
    @SerialName("nonce")
    public var nonce: String? = null,

    /**
     * Optional client-provided nonce that must match the client_nonce value provided during the prior request to the [auth_url](https://developer.hashicorp.com/vault/api-docs/auth/jwt#oidc-authorization-url-request) API.
     */
    @SerialName("client_nonce")
    public var clientNonce: String? = null,
) {

    /**
     * Builder class to simplify the creation of [OIDCCallbackPayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [OIDCCallbackPayload.state]
         */
        public lateinit var state: String

        /**
         * @see [OIDCCallbackPayload.code]
         */
        public lateinit var code: String

        /**
         * @see [OIDCCallbackPayload.nonce]
         */
        public var nonce: String? = null

        /**
         * @see [OIDCCallbackPayload.clientNonce]
         */
        public var clientNonce: String? = null

        /**
         * Build the instance of [OIDCCallbackPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): OIDCCallbackPayload = OIDCCallbackPayload(
            state = state,
            nonce = nonce,
            code = code,
            clientNonce = clientNonce,
        )
    }
}
