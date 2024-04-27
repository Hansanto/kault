package io.github.hansanto.kault.auth.token.response

import io.github.hansanto.kault.auth.common.common.TokenInfo
import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Build an instance of [TokenInfo] from the [TokenLookupResponse].
 * @receiver The response from the token lookup request.
 * @param token The token that was looked up.
 * @return All information about the token.
 */
public fun TokenLookupResponse.toTokenInfo(token: String): TokenInfo = TokenInfo(
    token = token,
    accessor = accessor,
    tokenPolicies = policies,
    metadata = metadata ?: emptyMap(),
    expirationDate = expireTime,
    renewable = renewable,
    entityId = entityId,
    tokenType = tokenType,
    orphan = orphan,
    numUses = numUses
)

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
    public val metadata: Map<String, String>?,

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
    public val tokenType: TokenType

)
