package io.github.hansanto.kault.system.auth.response

import io.github.hansanto.kault.auth.approle.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.system.auth.common.ListingVisibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AuthReadTuningInformationResponse(
    /**
     * Specifies the list of keys that will not be HMAC'd by audit devices in the request data object.
     */
    @SerialName("audit_non_hmac_request_keys")
    public val auditNonHmacRequestKeys: List<String>?,

    /**
     * List of headers to allow, allowing a plugin to include them in the response.
     */
    @SerialName("allowed_response_headers")
    public val allowedResponseHeaders: List<String>?,

    /**
     * Specifies the list of keys that will not be HMAC'd by audit devices in the response data object.
     */
    @SerialName("audit_non_hmac_response_keys")
    public val auditNonHmacResponseKeys: List<String>?,

    /**
     * List of headers to allow and pass from the request to the plugin.
     */
    @SerialName("passthrough_request_headers")
    public val passthroughRequestHeaders: List<String>?,

    @SerialName("allowed_managed_keys")
    public val allowedManagedKeys: List<String>?,

    /**
     * Specifies the default time-to-live. If set on a specific auth path, this overrides the global default.
     */
    @SerialName("default_lease_ttl")
    public val defaultLeaseTTL: VaultDuration,

    /**
     * Specifies the description of the mount. This overrides the current stored value, if any.
     */
    @SerialName("description")
    public val description: String,

    @SerialName("external_entropy_access")
    public val externalEntropyAccess: Boolean?,

    @SerialName("force_no_cache")
    public val forceNoCache: Boolean,

    /**
     * Specifies whether to show this mount in the UI-specific listing endpoint.
     */
    @SerialName("listing_visibility")
    public val listingVisibility: ListingVisibility?,

    /**
     * Specifies the maximum time-to-live. If set on a specific auth path, this overrides the global default.
     */
    @SerialName("max_lease_ttl")
    public val maxLeaseTTL: VaultDuration,

    /**
     * The options to pass into the backend.
     */
    @SerialName("options")
    public val options: Map<String, String>?,

    /**
     * Specifies the semantic version of the plugin to use, e.g. "v1.0.0". Changes will not take effect until the mount is reloaded.
     */
    @SerialName("plugin_version")
    public val pluginVersion: String?,

    /**
     * Specifies the type of tokens that should be returned by the mount.
     */
    @SerialName("token_type")
    public val tokenType: TokenType,

    /**
     * Specifies the duration after which the lockout counter is reset with no failed login attempts, specified as a string duration like "5s" or "30m".
     */
    @SerialName("user_lockout_counter_reset_duration")
    public val userLockoutCounterResetDuration: VaultDuration?,

    /**
     * Disables the user lockout feature for this mount if set to true.
     */
    @SerialName("user_lockout_disable")
    public val userLockoutDisable: Boolean?,

    /**
     * Specifies the duration for which an user will be locked out, specified as a string duration like "5s" or "30m".
     */
    @SerialName("user_lockout_duration")
    public val userLockoutDuration: VaultDuration?,

    /**
     * Specifies the number of failed login attempts after which the user is locked out, specified as a string like "15".
     */
    @SerialName("user_lockout_threshold")
    public val userLockoutThreshold: Long?
)
