package io.github.hansanto.kault.system.namespaces.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NamespacesLockResponse(
    /**
     * The key that can be used to unlock the namespace.
     */
    @SerialName("unlock_key")
    public val unlockKey: String
)
