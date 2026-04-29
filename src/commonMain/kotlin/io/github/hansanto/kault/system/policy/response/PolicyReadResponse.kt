package io.github.hansanto.kault.system.policy.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PolicyReadResponse(
    /**
     * The name of the policy.
     */
    @SerialName("name")
    val name: String,

    /**
     * The rules of the policy.
     */
    @SerialName("rules")
    val rules: String,
)
