package com.github.hansanto.kault.response

import kotlinx.serialization.Serializable

@Serializable
public data class StandardListResponse(val keys: List<String>)
