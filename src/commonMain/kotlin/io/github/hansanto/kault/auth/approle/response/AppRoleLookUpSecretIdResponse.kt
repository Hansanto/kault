package io.github.hansanto.kault.auth.approle.response

import io.github.hansanto.kault.serializer.VaultDuration
import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AppRoleLookUpSecretIdResponse(
    /**
     * List of CIDR blocks enforcing secret IDs to be used from specific set of IP addresses. If 'bound_cidr_list' is set on the role, then the list of CIDR blocks listed here should be a subset of the CIDR blocks listed on the role.
     */
    @SerialName("cidr_list")
    public val cidrList: List<String>,

    /**
     * Creation time of the secret ID.
     */
    @SerialName("creation_time")
    public val creationTime: Instant,

    /**
     * Expiration time of the secret ID.
     */
    @SerialName("expiration_time")
    public val expirationTime: Instant,

    /**
     * Last updated time of the secret ID.
     */
    @SerialName("last_updated_time")
    public val lastUpdatedTime: Instant,

    /**
     * Metadata of the secret ID.
     */
    @SerialName("metadata")
    public val metadata: Map<String, String>,

    /**
     * Accessor of the secret ID.
     */
    @SerialName("secret_id_accessor")
    public val secretIdAccessor: String,

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
     * List of CIDR blocks. If set, specifies the blocks of IP addresses which can use the returned token. Should be a subset of the token CIDR blocks listed on the role, if any.
     */
    @SerialName("token_bound_cidrs")
    public val tokenBoundCidrs: List<String>
)
