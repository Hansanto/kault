package io.github.hansanto.kault.auth.approle.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AppRoleReadRoleIdResponse(
    /**
     * Identifier of the role. Defaults to a UUID.
     */
    @SerialName("role_id")
    public val roleId: String
)
