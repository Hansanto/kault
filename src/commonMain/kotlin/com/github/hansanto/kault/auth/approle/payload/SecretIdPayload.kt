package com.github.hansanto.kault.auth.approle.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
public value class SecretIdPayload(
    @SerialName("secret_id")
    public val secretId: String
)

@JvmInline
@Serializable
public value class SecretIdAccessorPayload(
    @SerialName("secret_id_accessor")
    public val secretId: String
)
