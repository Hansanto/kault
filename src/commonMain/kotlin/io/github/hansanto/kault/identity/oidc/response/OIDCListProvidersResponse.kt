package io.github.hansanto.kault.identity.oidc.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCListProvidersResponse(
    /**
     * Information about each OIDC provider.
     */
    @SerialName("key_info")
    public val keyInfo: Map<String, OIDCReadProviderResponse>,
    /**
     * List of OIDC provider names.
     */
    @SerialName("keys")
    public val keys: List<String>,
)
