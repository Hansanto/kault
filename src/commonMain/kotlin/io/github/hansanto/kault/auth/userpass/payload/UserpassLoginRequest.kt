package io.github.hansanto.kault.auth.userpass.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class UserpassLoginRequest(
    /**
     * Password for this user.
     */
    @SerialName("password")
    public var password: String
)
