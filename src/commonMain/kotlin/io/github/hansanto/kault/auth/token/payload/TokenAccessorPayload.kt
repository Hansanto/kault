package io.github.hansanto.kault.auth.token.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public class TokenAccessorPayload(

    /**
     * Accessor value.
     */
    @SerialName("accessor")
    public var accessor: String
)
