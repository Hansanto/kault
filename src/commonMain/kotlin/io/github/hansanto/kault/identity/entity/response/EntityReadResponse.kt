package io.github.hansanto.kault.identity.entity.response

import kotlin.time.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class EntityReadResponse(
    @SerialName("aliases")
    val aliases: List<String>,

    @SerialName("creation_time")
    val creationTime: Instant,

    @SerialName("direct_group_ids")
    val directGroupIds: List<String>,

    @SerialName("disabled")
    val disabled: Boolean,

    @SerialName("group_ids")
    val groupIds: List<String>,

    @SerialName("id")
    val id: String,

    @SerialName("inherited_group_ids")
    val inheritedGroupIds: List<String>,

    @SerialName("last_update_time")
    val lastUpdateTime: Instant,

    @SerialName("merged_entity_ids")
    val mergedEntityIds: List<String>?,

    @SerialName("metadata")
    val metadata: Map<String, String>,

    @SerialName("name")
    val name: String,

    @SerialName("namespace_id")
    val namespaceId: String,

    @SerialName("policies")
    val policies: List<String>,
)
