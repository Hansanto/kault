package com.github.hansanto.kault.system.auth.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class EnableMethodPayload(
    /**
     * Specifies a human-friendly description of the auth method.
     */
    @SerialName("description")
    public var description: String? = null,

    /**
     * Specifies the name of the authentication method type, such as "github" or "token".
     */
    @SerialName("type")
    public var type: String? = null,

    /**
     * Specifies configuration options for this auth method. These are the possible values:
     */
    @SerialName("config")
    public var config: Config? = null,

    /**
     * Specifies if the auth method is local only. Local auth methods are not replicated nor (if a secondary) removed by replication. Local auth mounts also generate entities for tokens issued. The entity will be replicated across clusters and the aliases generated on the local auth mount will be local to the cluster. If the goal of marking an auth method as local was to comply with GDPR guidelines, then care must be taken to not set the data pertaining to local auth mount or local auth mount aliases in the metadata of the associated entity. Metadata related to local auth mount aliases can be stored as custom_metadata on the alias itself which will also be retained locally to the cluster.
     */
    @SerialName("local")
    public var local: Boolean? = null,

    /**
     * Enable seal wrapping for the mount, causing values stored by the mount to be wrapped by the seal's encryption capability.
     */
    @SerialName("seal_wrap")
    public var sealWrap: Boolean? = null
) {

    @Serializable
    public data class Config(
        /**
         * The default lease duration, specified as a string duration like "5s" or "30m".
         */
        @SerialName("default_lease_ttl")
        public var defaultLeaseTtl: String? = null,

        /**
         * The maximum lease duration, specified as a string duration like "5s" or "30m".
         */
        @SerialName("max_lease_ttl")
        public var maxLeaseTtl: String? = null,

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
    ) {

        @Serializable
        public enum class ListingVisibility(public val value: String) {

            /**
             * Mount is not visible.
             */
            HIDDEN("hidden"),

            /**
             * Mount is marked as internal-only.
             */
            UNAUTH("unauth");
        }
    }
}
