package io.github.hansanto.kault.system.mounts.response

import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.system.common.ListingVisibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MountsReadConfigurationResponse(
    /**
     * Specifies the description of the mount. This overrides the current stored value, if any.
     */
    @SerialName("description")
    val description: String,

    /**
     * Specifies the default time-to-live. This overrides the global default. A value of 0 is equivalent to the system default TTL.
     */
    @SerialName("default_lease_ttl")
    val defaultLeaseTTL: VaultDuration,

    /**
     * Specifies the maximum time-to-live. This overrides the global default. A value of 0 are equivalent and set to the system max TTL.
     */
    @SerialName("max_lease_ttl")
    val maxLeaseTTL: VaultDuration,

    /**
     * Disable caching.
     */
    @SerialName("force_no_cache")
    val forceNoCache: Boolean,

    /**
     * Specifies the list of keys that will not be HMAC'd by audit devices in the request data object.
     */
    @SerialName("audit_non_hmac_request_keys")
    val auditNonHmacRequestKeys: List<String>?,

    /**
     * Specifies the list of keys that will not be HMAC'd by audit devices in the response data object.
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
     * Specifies mount type specific options that are passed to the backend.
     */
    @SerialName("options")
    val options: Map<String, String>?,

    /**
     * Enable the secrets engine to access Vault's external entropy source.
     */
    @SerialName("external_entropy_access")
    val externalEntropyAccess: Boolean?,
)
