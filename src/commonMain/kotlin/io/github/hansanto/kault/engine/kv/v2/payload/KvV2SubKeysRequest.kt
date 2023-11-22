package io.github.hansanto.kault.engine.kv.v2.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class KvV2SubKeysRequest(
    /**
     * Specifies the version to return. If not set the latest version is returned.
     */
    @SerialName("version")
    public var version: Long? = null,

    /**
     * Specifies the deepest nesting level to provide in the output. The default value 0 will not impose any limit. If non-zero, keys that reside at the specified depth value will be artificially treated as leaves and will thus be null even if further underlying subkeys exist.
     */
    @SerialName("depth")
    public var depth: String? = null
)
