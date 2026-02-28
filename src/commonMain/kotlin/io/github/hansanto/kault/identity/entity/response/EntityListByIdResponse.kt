package io.github.hansanto.kault.identity.entity.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class EntityListByIdResponse(
    /**
     * Map of entity IDs to their names.
     */
    @SerialName("key_info")
    public val keyInfo: Map<String, EntityInfo>,
    /**
     * List of entity IDs.
     */
    @SerialName("keys")
    public val keys: List<String>,
) {

    @Serializable
    public data class EntityInfo(
        /**
         * Name of the entity.
         */
        @SerialName("name")
        public val name: String,
    )
}
