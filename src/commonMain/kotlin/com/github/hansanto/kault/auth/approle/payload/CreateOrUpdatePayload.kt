package com.github.hansanto.kault.auth.approle.payload

import com.github.hansanto.kault.auth.approle.common.TokenType
import com.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CreateOrUpdatePayload(
    /**
     * Require secret_id to be presented when logging in using this AppRole.
     */
    @SerialName("bind_secret_id")
    public var bindSecretId: Boolean? = null,

    /**
     * List of CIDR blocks; if set, specifies blocks of IP addresses which can perform the login operation.
     */
    @SerialName("secret_id_bound_cidrs")
    public var secretIdBoundCidrs: List<String>? = null,

    /**
     * Number of times any particular SecretID can be used to fetch a token from this AppRole, after which the SecretID by default will expire. A value of zero will allow unlimited uses. However, this option may be overridden by the request's 'num_uses' field when generating a SecretID.
     */
    @SerialName("secret_id_num_uses")
    public var secretIdNumUses: Long? = null,

    /**
     * Duration in either an integer number of seconds (3600) or an integer time unit (60m) after which by default any SecretID expires. A value of zero will allow the SecretID to not expire. However, this option may be overridden by the request's 'ttl' field when generating a SecretID.
     */
    @SerialName("secret_id_ttl")
    public var secretIdTTL: VaultDuration? = null,

    /**
     * If set, the secret IDs generated using this role will be cluster local. This can only be set during role creation and once set, it can't be reset later.
     */
    @SerialName("local_secret_ids")
    public var localSecretIds: Boolean? = null,

    /**
     * The incremental lifetime for generated tokens. This current value of this will be referenced at renewal time.
     */
    @SerialName("token_ttl")
    public var tokenTTL: VaultDuration? = null,

    /**
     * The maximum lifetime for generated tokens. This current value of this will be referenced at renewal time.
     */
    @SerialName("token_max_ttl")
    public var tokenMaxTTL: VaultDuration? = null,

    /**
     * List of token policies to encode onto generated tokens. Depending on the auth method, this list may be supplemented by user/group/other values.
     */
    @SerialName("token_policies")
    public var tokenPolicies: List<String>? = null,

    /**
     * List of CIDR blocks; if set, specifies blocks of IP addresses which can authenticate successfully, and ties the resulting token to these blocks as well.
     */
    @SerialName("token_bound_cidrs")
    public var tokenBoundCidrs: List<String>? = null,

    /**
     * If set, will encode an explicit max TTL onto the token. This is a hard cap even if token_ttl and token_max_ttl would otherwise allow a renewal.
     */
    @SerialName("token_explicit_max_ttl")
    public var tokenExplicitMaxTTL: VaultDuration? = null,

    /**
     * If set, the default policy will not be set on generated tokens; otherwise it will be added to the policies set in token_policies.
     */
    @SerialName("token_no_default_policy")
    public var tokenNoDefaultPolicy: Boolean? = null,

    /**
     * The maximum number of times a generated token may be used (within its lifetime); 0 means unlimited. If you require the token to have the ability to create child tokens, you will need to set this value to 0.
     */
    @SerialName("token_num_uses")
    public var tokenNumUses: Long? = null,

    /**
     * The maximum allowed period value when a periodic token is requested from this role.
     */
    @SerialName("token_period")
    public var tokenPeriod: VaultDuration? = null,

    /**
     * The type of token that should be generated.
     */
    @SerialName("token_type")
    public var tokenType: TokenType? = null
)
