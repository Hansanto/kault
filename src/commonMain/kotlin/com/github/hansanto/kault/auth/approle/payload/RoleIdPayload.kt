package com.github.hansanto.kault.auth.approle.payload

import kotlin.jvm.JvmInline
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@JvmInline
@Serializable
public value class RoleIdPayload(
    @SerialName("role_id")
    public val roleId: String
)
