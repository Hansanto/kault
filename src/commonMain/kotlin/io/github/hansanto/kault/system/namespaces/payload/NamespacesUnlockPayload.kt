package io.github.hansanto.kault.system.namespaces.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NamespacesUnlockPayload(
    /**
     * The key that can be used to unlock the namespace.
     */
    @SerialName("unlock_key")
    public var unlockKey: String
)
