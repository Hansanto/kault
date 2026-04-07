package io.github.hansanto.kault.auth.approle.response

import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AuthAppRoleReadResponse(
    /**
     * Impose secret ID to be presented when logging in using this role.
     */
    @SerialName("bind_secret_id")
    public val bindSecretId: Boolean,

    /**
     * If true, the secret identifiers generated using this role will be cluster local. This can only be set during role creation and once set, it can't be reset later
     */
    @SerialName("local_secret_ids")
    public val localSecretIds: Boolean,

    /**
     * List of CIDR blocks. If set, specifies the blocks of IP addresses which can perform the login operation.
     */
    @SerialName("secret_id_bound_cidrs")
    public val secretIdBoundCidrs: List<String>?,

    /**
     * Number of times a secret ID can access the role, after which the secret ID will expire.
     */
    @SerialName("secret_id_num_uses")
    public val secretIdNumUses: Long,

    /**
     * Duration in seconds after which the issued secret ID expires.
     */
    @SerialName("secret_id_ttl")
    public val secretIdTTL: VaultDuration,

    /**
     * List of CIDR blocks. If set, specifies the blocks of IP addresses which can perform the login operation.
     */
    @SerialName("token_bound_cidrs")
    public val tokenBoundCidrs: List<String>,

    /**
     * If set, tokens created via this role carry an explicit maximum TTL. During renewal, the current maximum TTL public values of the role and the mount are not checked for changes, and any updates to these public values will have no effect on the token being renewed.
     */
    @SerialName("token_explicit_max_ttl")
    public val tokenExplicitMaxTTL: VaultDuration,

    /**
     * The maximum lifetime of the generated token
     */
    @SerialName("token_max_ttl")
    public val tokenMaxTTL: VaultDuration,

    /**
     * If true, the 'default' policy will not automatically be added to generated tokens
     */
    @SerialName("token_no_default_policy")
    public val tokenNoDefaultPolicy: Boolean,

    /**
     * The maximum number of times a token may be used, a public value of zero means unlimited
     */
    @SerialName("token_num_uses")
    public val tokenNumUses: Long,

    /**
     * If set, tokens created via this role will have no max lifetime; instead, their renewal period will be fixed to this public value.
     */
    @SerialName("token_period")
    public val tokenPeriod: VaultDuration,

    /**
     * List of policies
     */
    @SerialName("token_policies")
    public val tokenPolicies: List<String>,

    /**
     * The initial ttl of the token to generate
     */
    @SerialName("token_ttl")
    public val tokenTTL: VaultDuration,

    /**
     * The type of token to generate.
     */
    @SerialName("token_type")
    public val tokenType: TokenType

)
