package io.github.hansanto.kault.system.auth.response

import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.system.auth.common.ListingVisibility
import io.github.hansanto.kault.system.auth.payload.EnableMethodPayload
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AuthReadConfigurationResponse(

    @SerialName("accessor")
    public val accessor: String,

    @SerialName("config")
    public val config: EnableMethodPayload.Config,

    @SerialName("deprecation_status")
    public val deprecationStatus: String?,

    @SerialName("description")
    public val description: String,

    @SerialName("external_entropy_access")
    public val externalEntropyAccess: Boolean,

    @SerialName("local")
    public val local: Boolean,

    @SerialName("options")
    public val options: Map<String, String>?,

    @SerialName("plugin_version")
    public val pluginVersion: String,

    @SerialName("running_plugin_version")
    public val runningPluginVersion: String,

    @SerialName("running_sha256")
    public val runningSha256: String,

    @SerialName("seal_wrap")
    public val sealWrap: Boolean,

    @SerialName("type")
    public val type: String,

    @SerialName("uuid")
    public val uuid: String
) {

    @Serializable
    public data class Config(
        /**
         * The default lease duration, specified as a string duration like "5s" or "30m".
         */
        @SerialName("default_lease_ttl")
        public var defaultLeaseTTL: VaultDuration,

        /**
         * The maximum lease duration, specified as a string duration like "5s" or "30m".
         */
        @SerialName("max_lease_ttl")
        public var maxLeaseTTL: VaultDuration,

        /**
         * Specifies the type of tokens that should be returned by the mount. The following values are available:
         *
         * default-service: Unless the auth method requests a different type, issue service tokens
         * default-batch: Unless the auth method requests a different type, issue batch tokens
         * service: Override any auth method preference and always issue service tokens from this mount
         * batch: Override any auth method preference and always issue batch tokens from this mount
         */
        @SerialName("token_type")
        public var tokenType: TokenType,

        @SerialName("force_no_cache")
        public var forceNoCache: Boolean,

        /**
         * List of keys that will not be HMAC'd by audit devices in the request data object.
         */
        @SerialName("audit_non_hmac_request_keys")
        public var auditNonHmacRequestKeys: List<String>? = null,

        /**
         * List of keys that will not be HMAC'd by audit devices in the response data object.
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
         * Specifies the semantic version of the plugin to use, e.g. "v1.0.0". If unspecified, the server will select any matching unversioned plugin that may have been registered, the latest versioned plugin registered, or a built-in plugin in that order of precendence.
         */
        @SerialName("plugin_version")
        public var pluginVersion: String? = null
    )
}
