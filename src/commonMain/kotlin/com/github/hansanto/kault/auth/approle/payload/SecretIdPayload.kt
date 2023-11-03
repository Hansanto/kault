package com.github.hansanto.kault.auth.approle.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class SecretIdPayload(
    @SerialName("secret_id")
    public val secretId: String
)

@Serializable
public class SecretIdAccessorPayload(
    @SerialName("secret_id_accessor")
    public val secretId: String
)
