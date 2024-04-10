package io.github.hansanto.kault.auth.token.response

import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TokenLookupResponse(
    @SerialName("accessor")
    public val accessor: String,

    @SerialName("creation_time")
    public val creationTime: Long,

    @SerialName("creation_ttl")
    public val creationTTL: VaultDuration,

    @SerialName("display_name")
    public val displayName: String,

    @SerialName("entity_id")
    public val entityId: String,

    @SerialName("expire_time")
    public val expireTime: Instant?,

    @SerialName("explicit_max_ttl")
    public val explicitMaxTTL: VaultDuration,

    @SerialName("id")
    public val id: String,

    @SerialName("issue_time")
    public val issueTime: Instant,

    @SerialName("meta")
    public val meta: Map<String, String>?,

    @SerialName("num_uses")
    public val numUses: Long,

    @SerialName("orphan")
    public val orphan: Boolean,

    @SerialName("path")
    public val path: String,

    @SerialName("policies")
    public val policies: List<String>,

    @SerialName("renewable")
    public val renewable: Boolean,

    @SerialName("ttl")
    public val ttl: VaultDuration,

    @SerialName("type")
    public val type: TokenType

)
