package io.github.hansanto.kault.auth.token.response

import io.github.hansanto.kault.auth.common.common.TokenInfo
import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.datetime.Clock
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Build an instance of [TokenInfo] from the [TokenCreateResponse].
 * @receiver The response from the renew token request.
 * @return All information about the new token.
 */
public fun TokenCreateResponse.toTokenInfo(): TokenInfo = TokenInfo(
    token = clientToken,
    accessor = accessor,
    tokenPolicies = tokenPolicies,
    metadata = metadata ?: emptyMap(),
    expirationDate = Clock.System.now().plus(leaseDuration),
    renewable = renewable,
    entityId = entityId,
    tokenType = tokenType,
    orphan = orphan,
    numUses = numUses
)

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
