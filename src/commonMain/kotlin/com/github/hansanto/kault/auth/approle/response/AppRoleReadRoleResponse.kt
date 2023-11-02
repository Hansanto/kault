package com.github.hansanto.kault.auth.approle.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AppRoleReadRoleResponse(
    /**
     * Impose secret ID to be presented when logging in using this role.
     */
    @SerialName("bind_secret_id")
    val bindSecretId: Boolean,

    /**
     * If true, the secret identifiers generated using this role will be cluster local. This can only be set during role creation and once set, it can't be reset later
     */
    @SerialName("local_secret_ids")
    val localSecretIds: Boolean,

    /**
     * List of CIDR blocks. If set, specifies the blocks of IP addresses which can perform the login operation.
     */
    @SerialName("secret_id_bound_cidrs")
    val secretIdBoundCidrs: List<String>?,

    /**
     * Number of times a secret ID can access the role, after which the secret ID will expire.
     */
    @SerialName("secret_id_num_uses")
    val secretIdNumUses: Long,

    /**
     * Duration in seconds after which the issued secret ID expires.
     */
    @SerialName("secret_id_ttl")
    val secretIdTTL: Long,

    /**
     * List of CIDR blocks. If set, specifies the blocks of IP addresses which can perform the login operation.
     */
    @SerialName("token_bound_cidrs")
    val tokenBoundCidrs: List<String>,

    /**
     * If set, tokens created via this role carry an explicit maximum TTL. During renewal, the current maximum TTL values of the role and the mount are not checked for changes, and any updates to these values will have no effect on the token being renewed.
     */
    @SerialName("token_explicit_max_ttl")
    val tokenExplicitMaxTTL: Long,

    /**
     * The maximum lifetime of the generated token
     */
    @SerialName("token_max_ttl")
    val tokenMaxTTL: Long,

    /**
     * If true, the 'default' policy will not automatically be added to generated tokens
     */
    @SerialName("token_no_default_policy")
    val tokenNoDefaultPolicy: Boolean,

    /**
     * The maximum number of times a token may be used, a value of zero means unlimited
     */
    @SerialName("token_num_uses")
    val tokenNumUses: Long,

    /**
     * If set, tokens created via this role will have no max lifetime; instead, their renewal period will be fixed to this value.
     */
    @SerialName("token_period")
    val tokenPeriod: Long,

    /**
     * List of policies
     */
    @SerialName("token_policies")
    val tokenPolicies: List<String>,

    /**
     * The initial ttl of the token to generate
     */
    @SerialName("token_ttl")
    val tokenTTL: Long,

    /**
     * The type of token to generate, service or batch
     */
    @SerialName("token_type")
    val tokenType: String

)
