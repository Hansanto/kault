package io.github.hansanto.kault.auth.oidc.response

import io.github.hansanto.kault.auth.oidc.common.OIDCResponseMode
import io.github.hansanto.kault.auth.oidc.common.OIDCResponseType
import io.github.hansanto.kault.auth.oidc.payload.OIDCConfigurePayload.JwksPair
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonPrimitive

@Serializable
public data class OIDCConfigureResponse(
    /**
     * The value against which to match the iss claim in a JWT. Cannot be configured when [jwksPairs] is set.
     */
    @SerialName("bound_issuer")
    public val boundIssuer: String,

    /**
     * The default role to use if none is provided during login.
     */
    @SerialName("default_role")
    public val defaultRole: String,

    /**
     * The contents of a CA certificate or chain of certificates, in PEM format, to use to validate connections to the JWKS URL. If not set, system certificates are used.
     */
    @SerialName("jwks_ca_pem")
    public val jwksCaPem: String,

    /**
     * List of JWKS URL and optional CA certificate pairs. CA certificates must be in PEM format. Cannot be used with [jwksUrl] or [jwksCaPem].
     */
    @SerialName("jwks_pairs")
    public val jwksPairs: List<JwksPair>,

    /**
     * JWKS URL to use to authenticate signatures. Cannot be used with [oidcDiscoveryUrl] or [jwtValidationPubkeys].
     */
    @SerialName("jwks_url")
    public val jwksUrl: String,

    /**
     * A list of supported signing algorithms. Defaults to RS256 for OIDC roles. Defaults to all [available algorithms](https://github.com/hashicorp/cap/blob/main/jwt/algs.go) for JWT roles.
     */
    @SerialName("jwt_supported_algs")
    public val jwtSupportedAlgorithms: List<String>,

    /**
     * A list of PEM-encoded public keys to use to authenticate signatures locally. Cannot be used with [jwksUrl] or [oidcDiscoveryUrl].
     */
    @SerialName("jwt_validation_pubkeys")
    public val jwtValidationPubkeys: List<String>,

    /**
     * Pass namespace in the OIDC state parameter instead of as a separate query parameter. With this setting, the allowed redirect URL(s) in Vault and on the provider side should not contain a namespace query parameter. This means only one redirect URL entry needs to be maintained on the provider side for all vault namespaces that will be authenticating against it. Defaults to true for new configs.
     */
    @SerialName("namespace_in_state")
    public val namespaceInState: Boolean,

    /**
     * The OAuth Client ID from the provider for OIDC roles.
     */
    @SerialName("oidc_client_id")
    public val oidcClientId: String,

    /**
     * The contents of a CA certificate or chain of certificates, in PEM format, to use to validate connections to the OIDC Discovery URL. If not set, system certificates are used.
     */
    @SerialName("oidc_discovery_ca_pem")
    public val oidcDiscoveryCaPem: String,

    /**
     * The OIDC Discovery URL, without any .well-known component (base path). Cannot be used with [jwksUrl] or [jwtValidationPubkeys].
     */
    @SerialName("oidc_discovery_url")
    public val oidcDiscoveryUrl: String,

    /**
     * The response mode to be used in the OAuth2 request. Defaults to [OIDCResponseMode.QUERY]. If using Vault namespaces, and oidc_response_mode is [OIDCResponseMode.FORM_POST], then [namespaceInState] should be set to false.
     */
    @SerialName("oidc_response_mode")
    public val oidcResponseMode: OIDCResponseMode,

    /**
     * The response types to request. Defaults to [OIDCResponseType.CODE]. Note: [OIDCResponseType.ID_TOKEN] may only be used if [oidcResponseMode] is set to [OIDCResponseMode.FORM_POST].
     */
    @SerialName("oidc_response_types")
    public val oidcResponseTypes: List<OIDCResponseType>,

    /**
     * Configuration options for provider-specific handling. Providers with specific handling include: Azure, Google, SecureAuth, IBM ISAM. The options are described in each provider's section in [OIDC Provider Setup](https://developer.hashicorp.com/vault/docs/auth/jwt/oidc-providers).
     */
    @SerialName("provider_config")
    public val providerConfig: Map<String, JsonPrimitive>
)
