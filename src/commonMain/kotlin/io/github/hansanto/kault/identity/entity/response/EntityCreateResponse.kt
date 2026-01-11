package io.github.hansanto.kault.identity.entity.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class EntityCreateResponse(
    /**
     * ID of the entity
     */
    @SerialName("id")
    val id: String,

    /**
     * Name of the entity.
     */
    @SerialName("name")
    val name: String,
)
