package io.github.hansanto.kault.identity.entity.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class EntityCreateOrUpdateByNamePayload(
    /**
     * Metadata to be associated with the entity.
     */
    @SerialName("metadata")
    public var metadata: Map<String, String>? = null,

    /**
     * Policies to be tied to the entity.
     */
    @SerialName("policies")
    public var policies: List<String>? = null,

    /**
     * Whether the entity is disabled. Disabled entities' associated tokens cannot be used, but are not revoked.
     */
    @SerialName("disabled")
    public var disabled: Boolean? = null,
)
