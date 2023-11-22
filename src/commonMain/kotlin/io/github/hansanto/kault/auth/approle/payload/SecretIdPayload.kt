package io.github.hansanto.kault.auth.approle.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class SecretIdPayload(

    /**
     * Secret ID attached to the role.
     */
    @SerialName("secret_id")
    public var secretId: String
)

@Serializable
public class SecretIdAccessorPayload(

    /**
     * Secret ID accessor attached to the role.
     */
    @SerialName("secret_id_accessor")
    public var secretId: String
)
