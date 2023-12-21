package io.github.hansanto.kault.auth.userpass.response

import io.github.hansanto.kault.auth.approle.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class ReadUserResponse(
    /**
     * The incremental lifetime for generated tokens. This current value of this will be referenced at renewal time.
     */
    @SerialName("token_ttl")
    public val tokenTTL: VaultDuration,

    /**
     * The maximum lifetime for generated tokens. This current value of this will be referenced at renewal time.
     */
    @SerialName("token_max_ttl")
    public val tokenMaxTTL: VaultDuration,

    /**
     * List of token policies to encode onto generated tokens. Depending on the auth method, this list may be supplemented by user/group/other values.
     */
    @SerialName("token_policies")
    public val tokenPolicies: List<String>,

    /**
     * List of CIDR blocks; if set, specifies blocks of IP addresses which can authenticate successfully, and ties the resulting token to these blocks as well.
     */
    @SerialName("token_bound_cidrs")
    public val tokenBoundCidrs: List<String>,

    /**
     * If set, will encode an explicit max TTL onto the token. This is a hard cap even if token_ttl and token_max_ttl would otherwise allow a renewal.
     */
    @SerialName("token_explicit_max_ttl")
    public val tokenExplicitMaxTTL: VaultDuration,

    /**
     * If set, the default policy will not be set on generated tokens; otherwise it will be added to the policies set in token_policies.
     */
    @SerialName("token_no_default_policy")
    public val tokenNoDefaultPolicy: Boolean,

    /**
     * The maximum number of times a generated token may be used (within its lifetime); 0 means unlimited. If you require the token to have the ability to create child tokens, you will need to set this value to 0.
     */
    @SerialName("token_num_uses")
    public val tokenNumUses: Long,

    /**
     * The maximum allowed period value when a periodic token is requested from this role.
     */
    @SerialName("token_period")
    public val tokenPeriod: VaultDuration,

    /**
     * The type of token that should be generated.
     */
    @SerialName("token_type")
    public val tokenType: TokenType
)
