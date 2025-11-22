package io.github.hansanto.kault.identity.oidc.payload

import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.identity.oidc.common.IdentityOIDCResponseType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCAuthorizationEndpointPayload(
    /**
     * A space-delimited list of scopes to be requested. The openid scope is required.
     */
    @SerialName("scope")
    public var scope: String,

    /**
     * The OIDC authentication flow to be used. The following response types are supported: code.
     */
    @SerialName("response_type")
    public var responseType: IdentityOIDCResponseType,

    /**
     * The ID of the requesting client.
     */
    @SerialName("client_id")
    public var clientId: String,

    /**
     * The redirection URI to which the response will be sent.
     */
    @SerialName("redirect_uri")
    public var redirectUri: String,

    /**
     * A value used to maintain state between the authentication request and client.
     */
    @SerialName("state")
    public var state: String,

    /**
     * A value that is returned in the ID token nonce claim. It is used to mitigate replay attacks, so we strongly encourage providing this optional parameter.
     */
    @SerialName("nonce")
    public var nonce: String? = null,

    /**
     * The allowable elapsed time in seconds since the last time the end-user was actively authenticated.
     */
    @SerialName("max_age")
    public var maxAge: Long? = null,

    /**
     * The [PKCE](https://datatracker.ietf.org/doc/html/rfc7636) code challenge derived from the client's code verifier. Optional for confidential clients. Required for public clients.
     */
    @SerialName("code_challenge")
    public var codeChallenge: String? = null,

    /**
     * TODO: Enum
     * The method that was used to derive the [PKCE](https://datatracker.ietf.org/doc/html/rfc7636) code challenge. The following methods are supported: S256, plain.
     */
    @SerialName("code_challenge_method")
    public var codeChallengeMethod: String? = null,
) {

    /**
     * Builder class to simplify the creation of [OIDCAuthorizationEndpointPayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [OIDCAuthorizationEndpointPayload.scope]
         */
        public lateinit var scope: String

        /**
         * @see [OIDCAuthorizationEndpointPayload.responseType]
         */
        public lateinit var responseType: IdentityOIDCResponseType

        /**
         * @see [OIDCAuthorizationEndpointPayload.clientId]
         */
        public lateinit var clientId: String

        /**
         * @see [OIDCAuthorizationEndpointPayload.redirectUri]
         */
        public lateinit var redirectUri: String

        /**
         * @see [OIDCAuthorizationEndpointPayload.state]
         */
        public lateinit var state: String

        /**
         * @see [OIDCAuthorizationEndpointPayload.nonce]
         */
        public var nonce: String? = null

        /**
         * @see [OIDCAuthorizationEndpointPayload.maxAge]
         */
        public var maxAge: Long? = null

        /**
         * @see [OIDCAuthorizationEndpointPayload.codeChallenge]
         */
        public var codeChallenge: String? = null

        /**
         * @see [OIDCAuthorizationEndpointPayload.codeChallengeMethod]
         */
        public var codeChallengeMethod: String? = null

        /**
         * Build the instance of [OIDCAuthorizationEndpointPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): OIDCAuthorizationEndpointPayload = OIDCAuthorizationEndpointPayload(
            scope = scope,
            responseType = responseType,
            clientId = clientId,
            redirectUri = redirectUri,
            state = state,
            nonce = nonce,
            maxAge = maxAge,
            codeChallenge = codeChallenge,
            codeChallengeMethod = codeChallengeMethod,
        )
    }
}
