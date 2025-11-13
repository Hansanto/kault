package io.github.hansanto.kault.identity.oidc.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCCreateOrUpdateProviderPayload(
    /**
     * Specifies what will be used as the scheme://host:port component for the iss claim of ID tokens. This defaults to a URL with Vault's api_addr as the scheme://host:port component and /v1/:namespace/identity/oidc/provider/:name as the path component. If provided explicitly, it must point to a Vault instance that is network reachable by clients for ID token validation.
     */
    @SerialName("issuer")
    public var issuer: String? = null,

    /**
     * The client IDs that are permitted to use the provider. If empty, no clients are allowed. If "*" is provided, all clients are allowed.
     */
    @SerialName("allowed_client_ids")
    public var allowedClientIds: List<String>? = null,

    /**
     * The scopes available for requesting on the provider.
     */
    @SerialName("scopes_supported")
    public var scopesSupported: List<String>? = null
)
