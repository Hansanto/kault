package io.github.hansanto.kault.auth.approle.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class AuthAppRoleReadSecretIdAccessorPayload(

    /**
     * Secret ID accessor attached to the role.
     */
    @SerialName("secret_id_accessor")
    public var secretId: String
)
