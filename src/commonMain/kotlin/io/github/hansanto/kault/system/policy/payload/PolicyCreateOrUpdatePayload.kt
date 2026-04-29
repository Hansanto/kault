package io.github.hansanto.kault.system.policy.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class PolicyCreateOrUpdatePayload(
    /**
     * The policy document.
     */
    @SerialName("policy")
    public var policy: String,
)
