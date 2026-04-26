package io.github.hansanto.kault.system.policy.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PolicyListResponse(
    /**
     * List of policies IDs.
     */
    @SerialName("keys")
    public val keys: List<String>,

    /**
     * List of policy names.
     */
    @SerialName("policies")
    public val policies: List<String>
)
