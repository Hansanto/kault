package io.github.hansanto.kault.auth.approle.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class AuthAppRoleSecretIdPayload(

    /**
     * Secret ID attached to the role.
     */
    @SerialName("secret_id")
    public var secretId: String
)
