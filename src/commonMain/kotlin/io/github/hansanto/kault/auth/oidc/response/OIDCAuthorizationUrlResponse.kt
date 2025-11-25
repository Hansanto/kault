package io.github.hansanto.kault.auth.oidc.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCAuthorizationUrlResponse(
    /**
     * The OIDC authorization URL to which the client should be redirected.
     */
    @SerialName("auth_url")
    public val authUrl: String,
)
