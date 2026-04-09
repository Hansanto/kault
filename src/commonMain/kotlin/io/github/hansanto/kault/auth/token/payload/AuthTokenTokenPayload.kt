package io.github.hansanto.kault.auth.token.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class AuthTokenTokenPayload(

    /**
     * Token value.
     */
    @SerialName("token")
    public var token: String
)
