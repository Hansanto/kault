package com.github.hansanto.kault.auth.approle.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class WriteSecretIdResponse(
    /**
     * Secret ID attached to the role.
     */
    @SerialName("secret_id")
    public val secretId: String,

    /**
     * Accessor of the secret ID
     */
    @SerialName("secret_id_accessor")
    public val secretIdAccessor: String,

    /**
     * Number of times a secret ID can access the role, after which the secret ID will expire.
     */
    @SerialName("secret_id_num_uses")
    public val secretIdNumUses: Int,

    /**
     * Duration in seconds after which the issued secret ID expires.
     */
    @SerialName("secret_id_ttl")
    public val secretIdTTL: Int
)
