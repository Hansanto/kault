package io.github.hansanto.kault.response

import kotlinx.serialization.Serializable

/**
 * Standard response to store a list of keys.
 */
@Serializable
public data class StandardListResponse(
    /**
     * The list of keys.
     */
    val keys: List<String>
)
