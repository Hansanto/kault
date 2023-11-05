package com.github.hansanto.kault.response

import kotlinx.serialization.Serializable

@Serializable
public data class StandardListResponse(
    /**
     * The list of keys.
     */
    val keys: List<String>
)
