package io.github.hansanto.kault.auth.token.response

import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TokenCreateResponse(
    @SerialName("client_token")
    public val clientToken: String,

    @SerialName("accessor")
    public val accessor: String,

    @SerialName("policies")
    public val policies: List<String>,

    @SerialName("token_policies")
    public val tokenPolicies: List<String>,

    @SerialName("metadata")
    public val metadata: Map<String, String>?,

    @SerialName("lease_duration")
    public val leaseDuration: VaultDuration,

    @SerialName("renewable")
    public val renewable: Boolean,

    @SerialName("entity_id")
    public val entityId: String,

    @SerialName("token_type")
    public val tokenType: TokenType,

    @SerialName("orphan")
    public val orphan: Boolean,

    @SerialName("num_uses")
    public val numUses: Long
)
