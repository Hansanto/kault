package io.github.hansanto.kault.auth.jwt.response

import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.auth.jwt.common.OIDCBoundClaimsType
import io.github.hansanto.kault.auth.jwt.common.OIDCRoleType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCReadRoleResponse(
    /**
     * The claim to use to uniquely identify the user; this will be used as the name for the Identity entity alias created due to a successful login. The claim value must be a string.
     */
    @SerialName("user_claim")
    public var userClaim: String,

    /**
     * The list of allowed values for redirect_uri during OIDC logins.
     */
    @SerialName("allowed_redirect_uris")
    public var allowedRedirectUris: List<String>,

    /**
     * Type of role.
     */
    @SerialName("role_type")
    public var roleType: OIDCRoleType,

    /**
     * List of aud claims to match against. The [boundAudiences] parameter is required for "jwt" roles that contain an audience (typical case) and must match at least one of the associated JWT 'aud' claims.
     */
    @SerialName("bound_audiences")
    public var boundAudiences: List<String>?,


    /**
     * Specifies if the [userClaim] value uses [JSON pointer](https://developer.hashicorp.com/vault/docs/auth/jwt#claim-specifications-and-json-pointer) syntax for referencing claims. By default, the [userClaim] value will not use JSON pointer.
     */
    @SerialName("user_claim_json_pointer")
    public var userClaimJsonPointer: Boolean,

    /**
     * The amount of leeway to add to all claims to account for clock skew, in seconds. Defaults to 60 seconds if set to 0 and can be disabled if set to -1. Accepts an integer number of seconds, or a Go duration format string. Only applicable with "jwt" roles.
     */
    @SerialName("clock_skew_leeway")
    public var clockSkewLeeway: VaultDuration,

    /**
     * The amount of leeway to add to expiration (exp) claims to account for clock skew, in seconds. Defaults to 150 seconds if set to 0 and can be disabled if set to -1. Accepts an integer number of seconds, or a Go duration format string. Only applicable with "jwt" roles.
     */
    @SerialName("expiration_leeway")
    public var expirationLeeway: VaultDuration,

    /**
     * The amount of leeway to add to not before (nbf) claims to account for clock skew, in seconds. Defaults to 150 seconds if set to 0 and can be disabled if set to -1. Accepts an integer number of seconds, or a Go duration format string. Only applicable with "jwt" roles.
     */
    @SerialName("not_before_leeway")
    public var notBeforeLeeway: VaultDuration,

    /**
     * If set, requires that the 'sub' claim matches this value.
     */
    @SerialName("bound_subject")
    public var boundSubject: String,

    /**
     * If set, a map of claims (keys) to match against respective claim values (values). Each expected value may be a string, integer, boolean or a list of strings. The interpretation of the bound claim values is configured with [boundClaimsType]. Keys support [JSON pointer](https://developer.hashicorp.com/vault/docs/auth/jwt#claim-specifications-and-json-pointer) syntax for referencing claims.
     */
    @SerialName("bound_claims")
    public var boundClaims: Map<String, String>?,

    /**
     * Configures the interpretation of the bound_claims values. If "string" (the default), the values will be treated as literals and must match exactly. If set to "glob", the values will be interpreted as globs, with * matching any number of characters.
     */
    @SerialName("bound_claims_type")
    public var boundClaimsType: OIDCBoundClaimsType,

    /**
     * The claim to use to uniquely identify the set of groups to which the user belongs; this will be used as the names for the Identity group aliases created due to a successful login. The claim value must be a list of strings. Supports [JSON pointer](https://developer.hashicorp.com/vault/docs/auth/jwt#claim-specifications-and-json-pointer) syntax for referencing claims.
     */
    @SerialName("groups_claim")
    public var groupsClaim: String,

    /**
     * If set, a map of claims (keys) to be copied to specified metadata fields (values). Keys support [JSON pointer](https://developer.hashicorp.com/vault/docs/auth/jwt#claim-specifications-and-json-pointer) syntax for referencing claims.
     */
    @SerialName("claim_mappings")
    public var claimMappings: Map<String, String>?,

    /**
     * If set, a list of OIDC scopes to be used with an OIDC role. The standard scope "openid" is automatically included and need not be specified.
     */
    @SerialName("oidc_scopes")
    public var oidcScopes: List<String>?,

    /**
     * Log received OIDC tokens and claims when debug-level logging is active. Not recommended in production since sensitive information may be present in OIDC responses.
     */
    @SerialName("verbose_oidc_logging")
    public var verboseOIDCLogging: Boolean,

    /**
     * Specifies the allowable elapsed time in seconds since the last time the user was actively authenticated with the OIDC provider. If set, the [maxAge] request parameter will be included in the authentication request. See [AuthRequest](https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest) for additional details. Accepts an integer number of seconds, or a Go duration format string.
     */
    @SerialName("max_age")
    public var maxAge: VaultDuration,

    /**
     * The incremental lifetime for generated tokens. This current value of this will be referenced at renewal time.
     */
    @SerialName("token_ttl")
    public var tokenTTL: VaultDuration,

    /**
     * The maximum lifetime for generated tokens. This current value of this will be referenced at renewal time.
     */
    @SerialName("token_max_ttl")
    public var tokenMaxTTL: VaultDuration,

    /**
     * List of token policies to encode onto generated tokens. Depending on the auth method, this list may be supplemented by user/group/other values.
     */
    @SerialName("token_policies")
    public var tokenPolicies: List<String>,

    /**
     * List of CIDR blocks; if set, specifies blocks of IP addresses which can authenticate successfully, and ties the resulting token to these blocks as well.
     */
    @SerialName("token_bound_cidrs")
    public var tokenBoundCidrs: List<String>,

    /**
     * If set, will encode an explicit max TTL onto the token. This is a hard cap even if token_ttl and token_max_ttl would otherwise allow a renewal.
     */
    @SerialName("token_explicit_max_ttl")
    public var tokenExplicitMaxTTL: VaultDuration,

    /**
     * If set, the default policy will not be set on generated tokens; otherwise it will be added to the policies set in token_policies.
     */
    @SerialName("token_no_default_policy")
    public var tokenNoDefaultPolicy: Boolean,

    /**
     * The maximum number of times a generated token may be used (within its lifetime); 0 means unlimited. If you require the token to have the ability to create child tokens, you will need to set this value to 0.
     */
    @SerialName("token_num_uses")
    public var tokenNumUses: Long,

    /**
     * The maximum allowed period value when a periodic token is requested from this role.
     */
    @SerialName("token_period")
    public var tokenPeriod: VaultDuration,

    /**
     * The type of token that should be generated.
     */
    @SerialName("token_type")
    public var tokenType: TokenType
)
