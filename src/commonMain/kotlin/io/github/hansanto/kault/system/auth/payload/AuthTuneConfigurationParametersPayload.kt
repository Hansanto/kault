package io.github.hansanto.kault.system.auth.payload

import io.github.hansanto.kault.auth.approle.common.AppRoleTokenType
import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.system.auth.common.ListingVisibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AuthTuneConfigurationParametersPayload(
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
     * List of headers to allow, allowing a plugin to include them in the response.
     */
    @SerialName("allowed_response_headers")
    public var allowedResponseHeaders: List<String>? = null,

    /**
     * List of headers to allow and pass from the request to the plugin.
     */
    @SerialName("passthrough_request_headers")
    public var passthroughRequestHeaders: List<String>? = null,

    /**
     * Specifies the default time-to-live. If set on a specific auth path, this overrides the global default.
     */
    @SerialName("default_lease_ttl")
    public var defaultLeaseTTL: VaultDuration? = null,

    /**
     * Specifies the description of the mount. This overrides the current stored value, if any.
     */
    @SerialName("description")
    public var description: String? = null,

    /**
     * Specifies whether to show this mount in the UI-specific listing endpoint.
     */
    @SerialName("listing_visibility")
    public var listingVisibility: ListingVisibility? = null,

    /**
     * Specifies the maximum time-to-live. If set on a specific auth path, this overrides the global default.
     */
    @SerialName("max_lease_ttl")
    public var maxLeaseTTL: VaultDuration? = null,

    /**
     * Specifies the semantic version of the plugin to use, e.g. "v1.0.0". Changes will not take effect until the mount is reloaded.
     */
    @SerialName("plugin_version")
    public var pluginVersion: String? = null,

    /**
     * The options to pass into the backend.
     */
    @SerialName("options")
    public var options: Map<String, String>? = null,

    /**
     * Specifies the type of tokens that should be returned by the mount.
     */
    @SerialName("token_type")
    public var tokenType: AppRoleTokenType? = null,

    /**
     * Specifies the user lockout configuration for the mount. User lockout feature was added in Vault 1.13.
     */
    @SerialName("user_lockout_config")
    public var userLockoutConfig: UserLockoutConfig? = null
) {

    @Serializable
    public data class UserLockoutConfig(
        /**
         * Specifies the number of failed login attempts after which the user is locked out, specified as a string like "15".
         */
        @SerialName("lockout_threshold")
        public var lockoutThreshold: String? = null,

        /**
         * Specifies the duration for which an user will be locked out, specified as a string duration like "5s" or "30m".
         */
        @SerialName("lockout_duration")
        public var lockoutDuration: VaultDuration? = null,

        /**
         * Specifies the duration after which the lockout counter is reset with no failed login attempts, specified as a string duration like "5s" or "30m".
         */
        @SerialName("lockout_counter_reset")
        public var lockoutCounterReset: VaultDuration? = null,

        /**
         * Disables the user lockout feature for this mount if set to true.
         */
        @SerialName("lockout_disable")
        public var lockoutDisable: Boolean? = null
    )
}
