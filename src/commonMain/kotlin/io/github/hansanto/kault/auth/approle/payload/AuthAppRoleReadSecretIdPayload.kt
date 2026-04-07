package io.github.hansanto.kault.auth.approle.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class AuthAppRoleReadSecretIdPayload(

    /**
     * Secret ID attached to the role.
     */
    @SerialName("secret_id")
    public var secretId: String
)
