package io.github.hansanto.kault.auth.token.response

import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TokenReadRoleResponse(
    @SerialName("allowed_entity_aliases")
    public val allowedEntityAliases: List<String>?,

    @SerialName("allowed_policies")
    public val allowedPolicies: List<String>,

    @SerialName("allowed_policies_glob")
    public val allowedPoliciesGlob: List<String>,

    @SerialName("disallowed_policies")
    public val disallowedPolicies: List<String>,

    @SerialName("disallowed_policies_glob")
    public val disallowedPoliciesGlob: List<String>,

    @SerialName("explicit_max_ttl")
    public val explicitMaxTTL: VaultDuration,

    @SerialName("name")
    public val name: String,

    @SerialName("orphan")
    public val orphan: Boolean,

    @SerialName("path_suffix")
    public val pathSuffix: String,

    @SerialName("period")
    public val period: VaultDuration,

    @SerialName("renewable")
    public val renewable: Boolean,

    @SerialName("token_explicit_max_ttl")
    public val tokenExplicitMaxTTL: VaultDuration,

    @SerialName("token_no_default_policy")
    public val tokenNoDefaultPolicy: Boolean,

    @SerialName("token_period")
    public val tokenPeriod: VaultDuration,

    @SerialName("token_type")
    public val tokenType: TokenType
)
