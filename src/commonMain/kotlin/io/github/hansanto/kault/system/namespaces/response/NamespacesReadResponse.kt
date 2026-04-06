package io.github.hansanto.kault.system.namespaces.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class NamespacesReadResponse(
    /**
     * Custom metadata associated with the namespace.
     */
    @SerialName("custom_metadata")
    public val customMetadata: Map<String, String>,

    /**
     * The ID of the namespace.
     */
    @SerialName("id")
    public val id: String,

    /**
     * `true` if the namespace is locked, `false` otherwise.
     */
    @SerialName("locked")
    public val locked: Boolean,

    /**
     * The path of the namespace.
     */
    @SerialName("path")
    public val path: String,

    /**
     * `true` if the namespace is tainted, `false` otherwise.
     */
    @SerialName("tainted")
    public val tainted: Boolean,

    /**
     * UUID of the namespace.
     */
    @SerialName("uuid")
    public val uuid: String
)
