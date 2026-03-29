package io.github.hansanto.kault.system.mounts.response

import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.system.common.ListingVisibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// TODO: Verify which fields are returned by the API.

@Serializable
public data class MountsReadConfigurationResponse(
    /**
     * Specifies the human-friendly description of the mount.
     */
    @SerialName("description")
    val description: String,

    /**
     * The default lease duration, specified as a string duration like "5s" or "30m".
     */
    @SerialName("default_lease_ttl")
    val defaultLeaseTTL: VaultDuration,

    /**
     * The maximum lease duration, specified as a string duration like "5s" or "30m".
     */
    @SerialName("max_lease_ttl")
    val maxLeaseTTL: VaultDuration,

    /**
     * Disable caching.
     */
    @SerialName("force_no_cache")
    val forceNoCache: Boolean,

    /**
     * List of keys that will not be HMAC'd by audit devices in the request data object.
     */
    @SerialName("audit_non_hmac_request_keys")
    val auditNonHmacRequestKeys: List<String>?,

    /**
     * List of keys that will not be HMAC'd by audit devices in the response data object.
     */
    @SerialName("audit_non_hmac_response_keys")
    val auditNonHmacResponseKeys: List<String>?,

    /**
     * Specifies whether to show this mount in the UI-specific listing endpoint.
     */
    @SerialName("listing_visibility")
    val listingVisibility: ListingVisibility?,

    /**
     * List of headers to allow and pass from the request to the plugin.
     */
    @SerialName("passthrough_request_headers")
    val passthroughRequestHeaders: List<String>?,

    /**
     * List of headers to allow, allowing a plugin to include them in the response.
     */
    @SerialName("allowed_response_headers")
    val allowedResponseHeaders: List<String>?,

    /**
     * List of managed key registry entry names that the mount in question is allowed to access.
     */
    @SerialName("allowed_managed_keys")
    val allowedManagedKeys: List<String>?,

    /**
     * List of allowed authentication mount accessors the backend can request delegated authentication for.
     */
    @SerialName("delegated_auth_accessors")
    val delegatedAuthAccessors: List<String>?,

    /**
     * The key to use for signing plugin workload identity tokens. If not provided, this will default to Vault's OIDC [default key](https://developer.hashicorp.com/vault/docs/concepts/oidc-provider#keys).
     */
    @SerialName("identity_token_key")
    val identityTokenKey: String?,

    /**
     * Specifies mount type specific options that are passed to the backend.
     */
    @SerialName("options")
    val options: Map<String, String>?,

    /**
     * Specifies if the secrets engine is a local mount only. Local mounts are not replicated nor (if a secondary) removed by replication.
     */
    @SerialName("local")
    val local: Boolean?,

    /**
     * Enable seal wrapping for the mount, causing values stored by the mount to be wrapped by the seal's encryption capability.
     */
    @SerialName("seal_wrap")
    val sealWrap: Boolean?,

    /**
     * Enable the secrets engine to access Vault's external entropy source.
     */
    @SerialName("external_entropy_access")
    val externalEntropyAccess: Boolean?,
)
