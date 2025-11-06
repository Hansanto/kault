package io.github.hansanto.kault.auth.jwt.payload

import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.auth.jwt.common.OIDCResponseMode
import io.github.hansanto.kault.auth.jwt.common.OIDCResponseType
import io.github.hansanto.kault.extension.toJsonPrimitiveMap
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
public data class OIDCConfigurePayload(
    /**
     * The value against which to match the iss claim in a JWT. Cannot be configured when [jwksPairs] is set.
     */
    @SerialName("bound_issuer")
    public var boundIssuer: String? = null,

    /**
     * The default role to use if none is provided during login.
     */
    @SerialName("default_role")
    public var defaultRole: String? = null,

    /**
     * The contents of a CA certificate or chain of certificates, in PEM format, to use to validate connections to the JWKS URL. If not set, system certificates are used.
     */
    @SerialName("jwks_ca_pem")
    public var jwksCaPem: String? = null,

    /**
     * List of JWKS URL and optional CA certificate pairs. CA certificates must be in PEM format. Cannot be used with [jwksUrl] or [jwksCaPem].
     */
    @SerialName("jwks_pairs")
    public var jwksPairs: List<JwksPair>? = null,

    /**
     * JWKS URL to use to authenticate signatures. Cannot be used with [oidcDiscoveryUrl] or [jwtValidationPubkeys].
     */
    @SerialName("jwks_url")
    public var jwksUrl: String? = null,

    /**
     * A list of supported signing algorithms. Defaults to RS256 for OIDC roles. Defaults to all [available algorithms](https://github.com/hashicorp/cap/blob/main/jwt/algs.go) for JWT roles.
     */
    @SerialName("jwt_supported_algs")
    public var jwtSupportedAlgorithms: List<String>? = null,

    /**
     * A list of PEM-encoded public keys to use to authenticate signatures locally. Cannot be used with [jwksUrl] or [oidcDiscoveryUrl].
     */
    @SerialName("jwt_validation_pubkeys")
    public var jwtValidationPubkeys: List<String>? = null,

    /**
     * Pass namespace in the OIDC state parameter instead of as a separate query parameter. With this setting, the allowed redirect URL(s) in Vault and on the provider side should not contain a namespace query parameter. This means only one redirect URL entry needs to be maintained on the provider side for all vault namespaces that will be authenticating against it. Defaults to true for new configs.
     */
    @SerialName("namespace_in_state")
    public var namespaceInState: Boolean? = null,

    /**
     * The OAuth Client ID from the provider for OIDC roles.
     */
    @SerialName("oidc_client_id")
    public var oidcClientId: String? = null,

    /**
     * The OAuth Client Secret from the provider for OIDC roles.
     */
    @SerialName("oidc_client_secret")
    public var oidcClientSecret: String? = null,

    /**
     * The contents of a CA certificate or chain of certificates, in PEM format, to use to validate connections to the OIDC Discovery URL. If not set, system certificates are used.
     */
    @SerialName("oidc_discovery_ca_pem")
    public var oidcDiscoveryCaPem: String? = null,

    /**
     * The OIDC Discovery URL, without any .well-known component (base path). Cannot be used with [jwksUrl] or [jwtValidationPubkeys].
     */
    @SerialName("oidc_discovery_url")
    public var oidcDiscoveryUrl: String? = null,

    /**
     * The response mode to be used in the OAuth2 request. Defaults to [io.github.hansanto.kault.auth.jwt.common.OIDCResponseMode.QUERY]. If using Vault namespaces, and oidc_response_mode is [io.github.hansanto.kault.auth.jwt.common.OIDCResponseMode.FORM_POST], then [namespaceInState] should be set to false.
     */
    @SerialName("oidc_response_mode")
    public var oidcResponseMode: OIDCResponseMode? = null,

    /**
     * The response types to request. Defaults to [io.github.hansanto.kault.auth.jwt.common.OIDCResponseType.CODE]. Note: [io.github.hansanto.kault.auth.jwt.common.OIDCResponseType.ID_TOKEN] may only be used if [oidcResponseMode] is set to [OIDCResponseMode.FORM_POST].
     */
    @SerialName("oidc_response_types")
    public var oidcResponseTypes: List<OIDCResponseType>? = null,

    /**
     * Configuration options for provider-specific handling. Providers with specific handling include: Azure, Google, SecureAuth, IBM ISAM. The options are described in each provider's section in [OIDC Provider Setup](https://developer.hashicorp.com/vault/docs/auth/jwt/oidc-providers).
     */
    @SerialName("provider_config")
    public var providerConfig: Map<String, JsonPrimitive>? = null,
) {

    @Serializable
    public data class JwksPair(
        /**
         * The JWKS URL.
         */
        @SerialName("jwks_url")
        public var jwksUrl: String,

        /**
         * The CA certificate or chain of certificates, in PEM format, to use to validate connections to the JWKS URL.
         * TODO: Check if optional
         */
        @SerialName("jwks_ca_pem")
        public var jwksCaPem: String?
    )

    /**
     * Builder class to simplify the creation of [OIDCConfigurePayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [OIDCConfigurePayload.boundIssuer]
         */
        public var boundIssuer: String? = null

        /**
         * @see [OIDCConfigurePayload.defaultRole]
         */
        public var defaultRole: String? = null

        /**
         * @see [OIDCConfigurePayload.jwksCaPem]
         */
        public var jwksCaPem: String? = null

        /**
         * @see [OIDCConfigurePayload.jwksPairs]
         */
        public var jwksPairs: List<JwksPair>? = null

        /**
         * @see [OIDCConfigurePayload.jwksUrl]
         */
        public var jwksUrl: String? = null

        /**
         * @see [OIDCConfigurePayload.jwtSupportedAlgorithms]
         */
        public var jwtSupportedAlgorithms: List<String>? = null

        /**
         * @see [OIDCConfigurePayload.jwtValidationPubkeys]
         */
        public var jwtValidationPubkeys: List<String>? = null

        /**
         * @see [OIDCConfigurePayload.namespaceInState]
         */
        public var namespaceInState: Boolean? = null

        /**
         * @see [OIDCConfigurePayload.oidcClientId]
         */
        public var oidcClientId: String? = null

        /**
         * @see [OIDCConfigurePayload.oidcClientSecret]
         */
        public var oidcClientSecret: String? = null

        /**
         * @see [OIDCConfigurePayload.oidcDiscoveryCaPem]
         */
        public var oidcDiscoveryCaPem: String? = null

        /**
         * @see [OIDCConfigurePayload.oidcDiscoveryUrl]
         */
        public var oidcDiscoveryUrl: String? = null

        /**
         * @see [OIDCConfigurePayload.oidcResponseMode]
         */
        public var oidcResponseMode: OIDCResponseMode? = null

        /**
         * @see [OIDCConfigurePayload.oidcResponseTypes]
         */
        public var oidcResponseTypes: List<OIDCResponseType>? = null

        /**
         * The map must have primitive values (null, boolean, number, string).
         * When building the payload with [build], the values will be converted to [JsonPrimitive]s automatically by the method [toJsonPrimitiveMap].
         * @see [OIDCConfigurePayload.providerConfig]
         */
        public var providerConfig: Map<String, Any?>? = null

        /**
         * Build the instance of [OIDCConfigurePayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): OIDCConfigurePayload = OIDCConfigurePayload(
            boundIssuer = boundIssuer,
            defaultRole = defaultRole,
            jwksCaPem = jwksCaPem,
            jwksPairs = jwksPairs,
            jwksUrl = jwksUrl,
            jwtSupportedAlgorithms = jwtSupportedAlgorithms,
            jwtValidationPubkeys = jwtValidationPubkeys,
            namespaceInState = namespaceInState,
            oidcClientId = oidcClientId,
            oidcClientSecret = oidcClientSecret,
            oidcDiscoveryCaPem = oidcDiscoveryCaPem,
            oidcDiscoveryUrl = oidcDiscoveryUrl,
            oidcResponseMode = oidcResponseMode,
            oidcResponseTypes = oidcResponseTypes,
            providerConfig = providerConfig?.toJsonPrimitiveMap(),
        )
    }
}
