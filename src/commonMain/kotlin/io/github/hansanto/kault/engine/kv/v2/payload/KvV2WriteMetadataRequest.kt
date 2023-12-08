package io.github.hansanto.kault.engine.kv.v2.payload

import io.github.hansanto.kault.common.SecretVersion
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class KvV2WriteMetadataRequest(

    /**
     * The number of versions to keep per key. If not set, the backend’s configured max version is used. Once a key has more than the configured allowed versions, the oldest version will be permanently deleted.
     */
    @SerialName("max_versions")
    var maxVersions: SecretVersion? = null,

    /**
     * If true, the key will require the cas parameter to be set on all write requests. If false, the backend’s configuration will be used.
     */
    @SerialName("cas_required")
    var casRequired: Boolean? = null,

    /**
     * Set the delete_version_after value to a duration to specify the deletion_time for all new versions written to this key. If not set, the backend's delete_version_after will be used. If the value is greater than the backend's delete_version_after, the backend's delete_version_after will be used. Accepts duration format strings.
     */
    @SerialName("delete_version_after")
    var deleteVersionAfter: VaultDuration? = null,

    /**
     * A map of arbitrary string to string valued user-provided metadata meant to describe the secret.
     */
    @SerialName("custom_metadata")
    var customMetadata: Map<String, String>? = null

)
