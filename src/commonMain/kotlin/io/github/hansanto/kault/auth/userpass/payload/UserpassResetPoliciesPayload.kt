package io.github.hansanto.kault.auth.userpass.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class UserpassResetPoliciesPayload(
    /**
     * A list of policies that will apply to the generated token for this user.
     */
    @SerialName("token_policies")
    public var tokenPolicies: List<String>
)
