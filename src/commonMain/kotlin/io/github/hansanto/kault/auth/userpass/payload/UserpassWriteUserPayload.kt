package io.github.hansanto.kault.auth.userpass.payload

import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.auth.approle.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class UserpassWriteUserPayload(

    /**
     * The password for the user. Only required when creating the user.
     */
    @SerialName("password")
    public var password: String,

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
) {

    /**
     * Builder class to simplify the creation of [UserpassWriteUserPayload].
     */
    @KaultDsl
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder {

        /**
         * @see [UserpassWriteUserPayload.password]
         */
        public lateinit var password: String

        /**
         * @see [UserpassWriteUserPayload.tokenTTL]
         */
        public var tokenTTL: VaultDuration? = null

        /**
         * @see [UserpassWriteUserPayload.tokenMaxTTL]
         */
        public var tokenMaxTTL: VaultDuration? = null

        /**
         * @see [UserpassWriteUserPayload.tokenPolicies]
         */
        public var tokenPolicies: List<String>? = null

        /**
         * @see [UserpassWriteUserPayload.tokenBoundCidrs]
         */
        public var tokenBoundCidrs: List<String>? = null

        /**
         * @see [UserpassWriteUserPayload.tokenExplicitMaxTTL]
         */
        public var tokenExplicitMaxTTL: VaultDuration? = null

        /**
         * @see [UserpassWriteUserPayload.tokenNoDefaultPolicy]
         */
        public var tokenNoDefaultPolicy: Boolean? = null

        /**
         * @see [UserpassWriteUserPayload.tokenNumUses]
         */
        public var tokenNumUses: Long? = null

        /**
         * @see [UserpassWriteUserPayload.tokenPeriod]
         */
        public var tokenPeriod: VaultDuration? = null

        /**
         * @see [UserpassWriteUserPayload.tokenType]
         */
        public var tokenType: TokenType? = null

        /**
         * Build the instance of [UserpassWriteUserPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): UserpassWriteUserPayload = UserpassWriteUserPayload(
            password = password,
            tokenTTL = tokenTTL,
            tokenMaxTTL = tokenMaxTTL,
            tokenPolicies = tokenPolicies,
            tokenBoundCidrs = tokenBoundCidrs,
            tokenExplicitMaxTTL = tokenExplicitMaxTTL,
            tokenNoDefaultPolicy = tokenNoDefaultPolicy,
            tokenNumUses = tokenNumUses,
            tokenPeriod = tokenPeriod,
            tokenType = tokenType
        )
    }
}
