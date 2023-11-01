package com.github.hansanto.kault.auth.approle.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

@JvmInline
@Serializable
public value class RoleIdPayload(
    @SerialName("role_id")
    public val roleId: String
)
