package io.github.hansanto.kault.identity.oidc.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCListClientsResponse(
    /**
     * Information about each OIDC client, keyed by client name.
     */
    @SerialName("key_info")
    public val keyInfo: Map<String, OIDCReadClientResponse>,
    /**
     * List of OIDC client names.
     */
    @SerialName("keys")
    public val keys: List<String>,
)
