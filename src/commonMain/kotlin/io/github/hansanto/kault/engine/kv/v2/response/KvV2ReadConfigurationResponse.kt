package io.github.hansanto.kault.engine.kv.v2.response

import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class KvV2ReadConfigurationResponse(

    /**
     * If true, the backend will require the cas parameter to be set for each write
     */
    @SerialName("cas_required")
    val casRequired: Boolean,

    /**
     * The length of time before a version is deleted.
     */
    @SerialName("delete_version_after")
    val deleteVersionAfter: VaultDuration,

    /**
     * The number of versions to keep for each key.
     */
    @SerialName("max_versions")
    val maxVersions: Long
)
