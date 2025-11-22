package io.github.hansanto.kault.identity.oidc.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Type corresponding to [OpenID Provider Configuration](https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderConfigurationResponse).
 * Know which fields are optional and which are mandatory: [OpenID Provider Metadata](https://openid.net/specs/openid-connect-discovery-1_0.html#ProviderMetadata).
 */
@Serializable
public data class OIDCReadProviderOpenIDConfigurationResponse(
    /**
     * URL using the https scheme with no query or fragment components that the OP asserts as its Issuer Identifier. If Issuer discovery is supported (see [Section 2](https://openid.net/specs/openid-connect-discovery-1_0.html#IssuerDiscovery)), this value MUST be identical to the issuer value returned by WebFinger. This also MUST be identical to the iss Claim value in ID Tokens issued from this Issuer.
     */
    @SerialName("issuer")
    public val issuer: String,

    /**
     * URL of the OP's OAuth 2.0 Authorization Endpoint [OpenID.Core](https://openid.net/specs/openid-connect-discovery-1_0.html#OpenID.Core). This URL MUST use the https scheme and MAY contain port, path, and query parameter components.
     */
    @SerialName("authorization_endpoint")
    public val authorizationEndpoint: String,

    /**
     * URL of the OP's JWK Set [JWK](https://openid.net/specs/openid-connect-discovery-1_0.html#JWK) document, which MUST use the https scheme. This contains the signing key(s) the RP uses to validate signatures from the OP. The JWK Set MAY also contain the Server's encryption key(s), which are used by RPs to encrypt requests to the Server. When both signing and encryption keys are made available, a use (public key use) parameter value is REQUIRED for all keys in the referenced JWK Set to indicate each key's intended usage. Although some algorithms allow the same key to be used for both signatures and encryption, doing so is NOT RECOMMENDED, as it is less secure. The JWK x5c parameter MAY be used to provide X.509 representations of keys provided. When used, the bare key values MUST still be present and MUST match those in the certificate. The JWK Set MUST NOT contain private or symmetric key values.
     */
    @SerialName("jwks_uri")
    public val jwksUri: String,

    /**
     * JSON array containing a list of the OAuth 2.0 response_type values that this OP supports. Dynamic OpenID Providers MUST support the code, id_token, and the id_token token Response Type values.
     */
    @SerialName("response_types_supported")
    public val responseTypesSupported: List<String>,

    /**
     * JSON array containing a list of the Subject Identifier types that this OP supports. Valid types include pairwise and public.
     */
    @SerialName("subject_types_supported")
    public val subjectTypesSupported: List<String>,

    /**
     * JSON array containing a list of the JWS signing algorithms (alg values) supported by the OP for the ID Token to encode the Claims in a [JWT](https://openid.net/specs/openid-connect-discovery-1_0.html#JWT). The algorithm RS256 MUST be included. The value none MAY be supported but MUST NOT be used unless the Response Type used returns no ID Token from the Authorization Endpoint (such as when using the Authorization Code Flow).
     */
    @SerialName("id_token_signing_alg_values_supported")
    public val idTokenSigningAlgValuesSupported: List<String>,

    /**
     * URL of the OP's OAuth 2.0 Token Endpoint [OpenID.Core](https://openid.net/specs/openid-connect-discovery-1_0.html#OpenID.Core). This is REQUIRED unless only the Implicit Flow is used. This URL MUST use the https scheme and MAY contain port, path, and query parameter components.
     */
    @SerialName("token_endpoint")
    public val tokenEndpoint: String? = null,

    /**
     * URL of the OP's UserInfo Endpoint [OpenID.Core](https://openid.net/specs/openid-connect-discovery-1_0.html#OpenID.Core). This URL MUST use the https scheme and MAY contain port, path, and query parameter components.
     */
    @SerialName("userinfo_endpoint")
    public val userinfoEndpoint: String? = null,

    /**
     * URL of the OP's Dynamic Client Registration Endpoint [OpenID.Registration](https://openid.net/specs/openid-connect-discovery-1_0.html#OpenID.Registration), which MUST use the https scheme.
     */
    @SerialName("registration_endpoint")
    public val registrationEndpoint: String? = null,

    /**
     * JSON array containing a list of the [OAuth 2.0](https://openid.net/specs/openid-connect-discovery-1_0.html#RFC6749) RFC6749 scope values that this server supports. The server MUST support the openid scope value. Servers MAY choose not to advertise some supported scope values even when this parameter is used, although those defined in [OpenID.Core](https://openid.net/specs/openid-connect-discovery-1_0.html#OpenID.Core) SHOULD be listed, if supported.
     */
    @SerialName("scopes_supported")
    public val scopesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the OAuth 2.0 response_mode values that this OP supports, as specified in [OAuth 2.0 Multiple Response Type Encoding Practices](https://openid.net/specs/openid-connect-discovery-1_0.html#OAuth.Responses). If omitted, the default for Dynamic OpenID Providers is ["query", "fragment"].
     */
    @SerialName("response_modes_supported")
    public val responseModesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the OAuth 2.0 Grant Type values that this OP supports. Dynamic OpenID Providers MUST support the authorization_code and implicit Grant Type values and MAY support other Grant Types. If omitted, the default value is ["authorization_code", "implicit"].
     */
    @SerialName("grant_types_supported")
    public val grantTypesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the Authentication Context Class References that this OP supports.
     */
    @SerialName("acr_values_supported")
    public val acrValuesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the JWE encryption algorithms (alg values) supported by the OP for the ID Token to encode the Claims in a [JWT](https://openid.net/specs/openid-connect-discovery-1_0.html#JWT).
     */
    @SerialName("id_token_encryption_alg_values_supported")
    public val idTokenEncryptionAlgValuesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the JWE encryption algorithms (enc values) supported by the OP for the ID Token to encode the Claims in a [JWT](https://openid.net/specs/openid-connect-discovery-1_0.html#JWT).
     */
    @SerialName("id_token_encryption_enc_values_supported")
    public val idTokenEncryptionEncValuesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the [JWS](https://openid.net/specs/openid-connect-discovery-1_0.html#JWS) signing algorithms (alg values) [JWA](https://openid.net/specs/openid-connect-discovery-1_0.html#JWA) supported by the UserInfo Endpoint to encode the Claims in a [JWT](https://openid.net/specs/openid-connect-discovery-1_0.html#JWT). The value none MAY be included.
     */
    @SerialName("userinfo_signing_alg_values_supported")
    public val userinfoSigningAlgValuesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the [JWE](https://openid.net/specs/openid-connect-discovery-1_0.html#JWE) encryption algorithms (alg values) [JWA](https://openid.net/specs/openid-connect-discovery-1_0.html#JWA) supported by the UserInfo Endpoint to encode the Claims in a [JWT](https://openid.net/specs/openid-connect-discovery-1_0.html#JWT).
     */
    @SerialName("userinfo_encryption_alg_values_supported")
    public val userinfoEncryptionAlgValuesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the JWE encryption algorithms (enc values) [JWA](https://openid.net/specs/openid-connect-discovery-1_0.html#JWA) supported by the UserInfo Endpoint to encode the Claims in a [JWT](https://openid.net/specs/openid-connect-discovery-1_0.html#JWT).
     */
    @SerialName("userinfo_encryption_enc_values_supported")
    public val userinfoEncryptionEncValuesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the JWS signing algorithms (alg values) supported by the OP for Request Objects, which are described in Section 6.1 of [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-discovery-1_0.html#OpenID.Core). These algorithms are used both when the Request Object is passed by value (using the request parameter) and when it is passed by reference (using the request_uri parameter). Servers SHOULD support none and RS256.
     */
    @SerialName("request_object_signing_alg_values_supported")
    public val requestObjectSigningAlgValuesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the JWE encryption algorithms (alg values) supported by the OP for Request Objects. These algorithms are used both when the Request Object is passed by value and when it is passed by reference.
     */
    @SerialName("request_object_encryption_alg_values_supported")
    public val requestObjectEncryptionAlgValuesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the JWE encryption algorithms (enc values) supported by the OP for Request Objects. These algorithms are used both when the Request Object is passed by value and when it is passed by reference.
     */
    @SerialName("request_object_encryption_enc_values_supported")
    public val requestObjectEncryptionEncValuesSupported: List<String>? = null,

    /**
     * JSON array containing a list of Client Authentication methods supported by this Token Endpoint. The options are client_secret_post, client_secret_basic, client_secret_jwt, and private_key_jwt, as described in Section 9 of [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-discovery-1_0.html#OpenID.Core). Other authentication methods MAY be defined by extensions. If omitted, the default is client_secret_basic -- the HTTP Basic Authentication Scheme specified in Section 2.3.1 of [OAuth 2.0](https://openid.net/specs/openid-connect-discovery-1_0.html#RFC6749) RFC6749.
     */
    @SerialName("token_endpoint_auth_methods_supported")
    public val tokenEndpointAuthMethodsSupported: List<String>? = null,

    /**
     * JSON array containing a list of the JWS signing algorithms (alg values) supported by the Token Endpoint for the signature on the [JWT](https://openid.net/specs/openid-connect-discovery-1_0.html#JWT) used to authenticate the Client at the Token Endpoint for the private_key_jwt and client_secret_jwt authentication methods. Servers SHOULD support RS256. The value none MUST NOT be used.
     */
    @SerialName("token_endpoint_auth_signing_alg_values_supported")
    public val tokenEndpointAuthSigningAlgValuesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the display parameter values that the OpenID Provider supports. These values are described in Section 3.1.2.1 of [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-discovery-1_0.html#OpenID.Core).
     */
    @SerialName("display_values_supported")
    public val displayValuesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the Claim Types that the OpenID Provider supports. These Claim Types are described in Section 5.6 of [OpenID Connect Core 1.0](https://openid.net/specs/openid-connect-discovery-1_0.html#OpenID.Core). Values defined by this specification are normal, aggregated, and distributed. If omitted, the implementation supports only normal Claims.
     */
    @SerialName("claim_types_supported")
    public val claimTypesSupported: List<String>? = null,

    /**
     * JSON array containing a list of the Claim Names of the Claims that the OpenID Provider MAY be able to supply values for. Note that for privacy or other reasons, this might not be an exhaustive list.
     */
    @SerialName("claims_supported")
    public val claimsSupported: List<String>? = null,

    /**
     * URL of a page containing human-readable information that developers might want or need to know when using the OpenID Provider. In particular, if the OpenID Provider does not support Dynamic Client Registration, then information on how to register Clients needs to be provided in this documentation.
     */
    @SerialName("service_documentation")
    public val serviceDocumentation: String? = null,

    /**
     * Languages and scripts supported for values in Claims being returned, represented as a JSON array of [BCP47](https://openid.net/specs/openid-connect-discovery-1_0.html#RFC5646) RFC5646 language tag values. Not all languages and scripts are necessarily supported for all Claim values.
     */
    @SerialName("claims_locales_supported")
    public val claimsLocalesSupported: List<String>? = null,

    /**
     * Languages and scripts supported for the user interface, represented as a JSON array of [BCP47](https://openid.net/specs/openid-connect-discovery-1_0.html#RFC5646) RFC5646 language tag values.
     */
    @SerialName("ui_locales_supported")
    public val uiLocalesSupported: List<String>? = null,

    /**
     * Boolean value specifying whether the OP supports use of the claims parameter, with true indicating support. If omitted, the default value is false.
     */
    @SerialName("claims_parameter_supported")
    public val claimsParameterSupported: Boolean? = null,

    /**
     * Boolean value specifying whether the OP supports use of the request parameter, with true indicating support. If omitted, the default value is false.
     */
    @SerialName("request_parameter_supported")
    public val requestParameterSupported: Boolean? = null,

    /**
     * Boolean value specifying whether the OP supports use of the request_uri parameter, with true indicating support. If omitted, the default value is true.
     */
    @SerialName("request_uri_parameter_supported")
    public val requestUriParameterSupported: Boolean? = null,

    /**
     * Boolean value specifying whether the OP requires any request_uri values used to be pre-registered using the request_uris registration parameter. Pre-registration is REQUIRED when the value is true. If omitted, the default value is false.
     */
    @SerialName("require_request_uri_registration")
    public val requireRequestUriRegistration: Boolean? = null,

    /**
     * URL that the OpenID Provider provides to the person registering the Client to read about the OP's requirements on how the Relying Party can use the data provided by the OP. The registration process SHOULD display this URL to the person registering the Client if it is given.
     */
    @SerialName("op_policy_uri")
    public val opPolicyUri: String? = null,

    /**
     * URL that the OpenID Provider provides to the person registering the Client to read about the OpenID Provider's terms of service. The registration process SHOULD display this URL to the person registering the Client if it is given.
     */
    @SerialName("op_tos_uri")
    public val opTosUri: String? = null,

    @SerialName("check_session_iframe")
    public val checkSessionIframe: String? = null,

    @SerialName("end_session_endpoint")
    public val endSessionEndpoint: String? = null,

    )
