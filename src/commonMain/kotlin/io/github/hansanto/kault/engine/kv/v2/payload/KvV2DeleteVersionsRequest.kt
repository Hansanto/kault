package io.github.hansanto.kault.engine.kv.v2.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class KvV2DeleteVersionsRequest(
    /**
     * The versions to be archived. The versioned data will not be deleted, but it will no longer be returned in normal get requests.
     */
    @SerialName("versions")
    var versions: List<Long>
)
