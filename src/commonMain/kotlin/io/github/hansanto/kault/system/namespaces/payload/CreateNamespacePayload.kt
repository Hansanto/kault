package io.github.hansanto.kault.system.namespaces.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class CreateNamespacePayload(
    /**
     * A map of arbitrary string to string valued user-provided metadata meant to describe the namespace.
     */
    @SerialName("custom_metadata")
    public var customMetadata: Map<String, String>? = null,
)
