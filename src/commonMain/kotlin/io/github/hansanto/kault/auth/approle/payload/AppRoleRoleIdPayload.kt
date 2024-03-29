package io.github.hansanto.kault.auth.approle.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class AppRoleRoleIdPayload(

    /**
     * Identifier of the role.
     */
    @SerialName("role_id")
    public var roleId: String
)
