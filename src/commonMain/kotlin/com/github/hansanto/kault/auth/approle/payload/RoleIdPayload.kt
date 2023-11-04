package com.github.hansanto.kault.auth.approle.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class RoleIdPayload(

    /**
     * Identifier of the role.
     */
    @SerialName("role_id")
    public val roleId: String
)
