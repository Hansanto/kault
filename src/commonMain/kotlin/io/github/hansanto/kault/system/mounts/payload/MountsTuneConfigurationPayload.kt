package io.github.hansanto.kault.system.mounts.payload

import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.system.common.ListingVisibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MountsTuneConfigurationPayload(
    /**
     * Specifies the default time-to-live. This overrides the global default. A value of 0 is equivalent to the system default TTL.
     */
    @SerialName("default_lease_ttl")
    public var defaultLeaseTTL: VaultDuration? = null,

    /**
     * Specifies the maximum time-to-live. This overrides the global default. A value of 0 are equivalent and set to the system max TTL.
     */
    @SerialName("max_lease_ttl")
    public var maxLeaseTTL: VaultDuration? = null,

    /**
     * Specifies the description of the mount. This overrides the current stored value, if any.
     */
    @SerialName("description")
    public var description: String? = null,

    /**
     * Specifies the list of keys that will not be HMAC'd by audit devices in the request data object.
     */
    @SerialName("audit_non_hmac_request_keys")
    public var auditNonHmacRequestKeys: List<String>? = null,

    /**
     * Specifies the list of keys that will not be HMAC'd by audit devices in the response data object.
     */
    @SerialName("audit_non_hmac_response_keys")
    public var auditNonHmacResponseKeys: List<String>? = null,

    /**
     * Specifies whether to show this mount in the UI-specific listing endpoint.
     */
    @SerialName("listing_visibility")
    public var listingVisibility: ListingVisibility? = null,

    /**
     * List of headers to allow and pass from the request to the plugin.
     */
    @SerialName("passthrough_request_headers")
    public var passthroughRequestHeaders: List<String>? = null,

    /**
     * List of headers to allow, allowing a plugin to include them in the response.
     */
    @SerialName("allowed_response_headers")
    public var allowedResponseHeaders: List<String>? = null,

    /**
     * List of managed key registry entry names that the mount in question is allowed to access.
     */
    @SerialName("allowed_managed_keys")
    public var allowedManagedKeys: List<String>? = null,

    /**
     * Specifies the semantic version of the plugin to use, e.g. "v1.0.0". Changes will not take effect until the mount is reloaded.
     */
    @SerialName("plugin_version")
    public var pluginVersion: String? = null,

    /**
     * List of allowed authentication mount accessors the backend can request delegated authentication for.
     */
    @SerialName("delegated_auth_accessors")
    public var delegatedAuthAccessors: List<String>? = null,
)
