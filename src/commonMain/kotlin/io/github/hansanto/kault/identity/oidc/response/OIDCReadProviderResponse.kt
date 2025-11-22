package io.github.hansanto.kault.identity.oidc.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCReadProviderResponse(
    /**
     * Specifies what will be used as the scheme://host:port component for the iss claim of ID tokens. This defaults to a URL with Vault's api_addr as the scheme://host:port component and /v1/:namespace/identity/oidc/provider/:name as the path component. If provided explicitly, it must point to a Vault instance that is network reachable by clients for ID token validation.
     */
    @SerialName("allowed_client_ids")
    public val allowedClientIds: List<String>,

    /**
     * The client IDs that are permitted to use the provider. If empty, no clients are allowed. If "*" is provided, all clients are allowed.
     */
    @SerialName("issuer")
    public val issuer: String,

    /**
     * The scopes available for requesting on the provider.
     */
    @SerialName("scopes_supported")
    public val scopesSupported: List<String>,
)
