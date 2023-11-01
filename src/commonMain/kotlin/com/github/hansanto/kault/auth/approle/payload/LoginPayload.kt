package com.github.hansanto.kault.auth.approle.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class LoginPayload(

    @SerialName("role_id")
    public val roleId: String,

    @SerialName("secret_id")
    public val secretId: String
)
