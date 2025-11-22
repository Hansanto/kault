package io.github.hansanto.kault.identity.oidc.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCAuthorizationEndpointResponse(
    /**
     * The authorization code returned from the authorization endpoint.
     */
    @SerialName("code")
    val code: String,

    /**
     * The state parameter is used to maintain state between the request and the callback.
     */
    @SerialName("state")
    val state: String?,
)
