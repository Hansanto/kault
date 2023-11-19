package io.github.hansanto.kault.engine.kv.v2.payload

import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class KvV2ConfigureRequest(

    /**
     * If true all keys will require the cas parameter to be set on all write requests.
     */
    @SerialName("cas-required")
    val casRequired: Boolean? = null,

    /**
     * If set, specifies the length of time before a version is deleted.
     */
    @SerialName("delete_version_after")
    val deleteVersionAfter: VaultDuration? = null,

    /**
     * The number of versions to keep per key. This value applies to all keys, but a key's metadata setting can overwrite this value. Once a key has more than the configured allowed versions, the oldest version will be permanently deleted. When 0 is used or the value is unset, Vault will keep 10 versions.
     */
    @SerialName("max_versions")
    val maxVersions: Long? = null
)
