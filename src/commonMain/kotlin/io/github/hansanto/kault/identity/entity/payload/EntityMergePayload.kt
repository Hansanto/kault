package io.github.hansanto.kault.identity.entity.payload

import io.github.hansanto.kault.KaultDsl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class EntityMergePayload(
    /**
     * Entity IDs which need to get merged.
     */
    @SerialName("from_entity_ids")
    public var fromEntityIds: List<String>,

    /**
     * Entity ID into which all the other entities need to get merged.
     */
    @SerialName("to_entity_id")
    public var toEntityId: String,

    /**
     * Setting this will follow the 'mine' strategy for merging MFA secrets. If there are secrets of the same type both in entities that are merged from and in entity into which all others are getting merged, secrets in the destination will be unaltered. If not set, this API will throw an error containing all the conflicts.
     */
    @SerialName("force")
    public var force: Boolean? = null,

    /**
     * A list of entity aliases to keep in the case where the to-Entity and from-Entity have aliases with the same mount accessor. In the case where alias share mount accessors, the alias ID given in this list will be kept or merged, and the other alias will be deleted. Note that merges requiring this parameter must have only one from-Entity.
     */
    @SerialName("conflicting_alias_ids_to_keep")
    public var conflictingAliasIdsToKeep: List<String>? = null,
) {

    /**
     * Builder class to simplify the creation of [EntityMergePayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [EntityMergePayload.fromEntityIds]
         */
        public lateinit var fromEntityIds: List<String>

        /**
         * @see [EntityMergePayload.toEntityId]
         */
        public lateinit var toEntityId: String

        /**
         * @see [EntityMergePayload.force]
         */
        public var force: Boolean? = null

        /**
         * @see [EntityMergePayload.conflictingAliasIdsToKeep]
         */
        public var conflictingAliasIdsToKeep: List<String>? = null

        /**
         * Build the instance of [EntityMergePayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): EntityMergePayload = EntityMergePayload(
            fromEntityIds = fromEntityIds,
            toEntityId = toEntityId,
            force = force,
            conflictingAliasIdsToKeep = conflictingAliasIdsToKeep,
        )
    }
}
