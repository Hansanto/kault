package io.github.hansanto.kault.engine.kv.v2.response

import io.github.hansanto.kault.serializer.VaultDuration
import io.ktor.http.content.Version
import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
public data class KvV2ReadMetadataResponse(

    @SerialName("cas_required")
    val casRequired: Boolean,

    @SerialName("created_time")
    val createdTime: Instant,

    @SerialName("current_version")
    val currentVersion: Long,

    @SerialName("custom_metadata")
    val customMetadata: JsonObject?,

    @SerialName("delete_version_after")
    val deleteVersionAfter: VaultDuration,

    @SerialName("max_versions")
    val maxVersions: Long,

    @SerialName("oldest_version")
    val oldestVersion: Long,

    @SerialName("updated_time")
    val updatedTime: Instant,

    @SerialName("versions")
    val versions: Map<Long, Version>
) {
    @Serializable
    public data class Version(

        /**
         * The time at which the version was created.
         */
        @SerialName("created_time")
        val createdTime: Instant,

        /**
         * The time at which the version was deleted.
         */
        @SerialName("deletion_time")
        val deletionTime: Instant,

        /**
         * True if the version is destroyed.
         */
        @SerialName("destroyed")
        val destroyed: Boolean
    )
}
