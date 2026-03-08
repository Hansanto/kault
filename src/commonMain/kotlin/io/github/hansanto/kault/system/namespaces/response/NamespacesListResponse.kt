package io.github.hansanto.kault.system.namespaces.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NamespacesListResponse(
    /**
     * Map of entity IDs to their names.
     */
    @SerialName("key_info")
    public val keyInfo: Map<String, NamespacesReadResponse>,
    /**
     * List of entity IDs.
     */
    @SerialName("keys")
    public val keys: List<String>,
)
