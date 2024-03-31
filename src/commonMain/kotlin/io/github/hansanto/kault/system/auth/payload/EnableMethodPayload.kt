package io.github.hansanto.kault.system.auth.payload

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.system.auth.common.ListingVisibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class EnableMethodPayload(
    /**
     * Specifies the name of the authentication method type, such as "github" or "token".
     */
    @SerialName("type")
    public var type: String,

    /**
     * Specifies configuration options for this auth method. These are the possible values:
     */
    @SerialName("config")
    public var config: Config? = null,

    /**
     * Specifies a human-friendly description of the auth method.
     */
    @SerialName("description")
    public var description: String? = null,

    /**
     * Whether to give the mount access to Vault's external entropy.
     */
    @SerialName("external_entropy_access")
    public var externalEntropyAccess: Boolean? = null,

    /**
     * Specifies if the auth method is local only. Local auth methods are not replicated nor (if a secondary) removed by replication. Local auth mounts also generate entities for tokens issued. The entity will be replicated across clusters and the aliases generated on the local auth mount will be local to the cluster. If the goal of marking an auth method as local was to comply with GDPR guidelines, then care must be taken to not set the data pertaining to local auth mount or local auth mount aliases in the metadata of the associated entity. Metadata related to local auth mount aliases can be stored as custom_metadata on the alias itself which will also be retained locally to the cluster.
     */
    @SerialName("local")
    public var local: Boolean? = null,

    /**
     * The options to pass into the backend. Should be a json object with string keys and values.
     */
    @SerialName("options")
    public var options: Map<String, String>? = null,

    /**
     * Name of the auth plugin to use based from the name in the plugin catalog.
     */
    @SerialName("plugin_name")
    public var pluginName: String? = null,

    /**
     * The semantic version of the plugin to use, or image tag if oci_image is provided.
     */
    @SerialName("plugin_version")
    public var pluginVersion: String? = null,

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
        public var defaultLeaseTTL: VaultDuration? = null,

        /**
         * The maximum lease duration, specified as a string duration like "5s" or "30m".
         */
        @SerialName("max_lease_ttl")
        public var maxLeaseTTL: VaultDuration? = null,

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

    /**
     * Builder class to simplify the creation of [EnableMethodPayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [EnableMethodPayload.type]
         */
        public lateinit var type: String

        /**
         * Builder to define the [Config] of [EnableMethodPayload].
         */
        private var configBuilder: BuilderDsl<Config>? = null

        /**
         * @see [EnableMethodPayload.description]
         */
        public var description: String? = null

        /**
         * @see [EnableMethodPayload.externalEntropyAccess]
         */
        public var externalEntropyAccess: Boolean? = null

        /**
         * @see [EnableMethodPayload.local]
         */
        public var local: Boolean? = null

        /**
         * @see [EnableMethodPayload.options]
         */
        public var options: Map<String, String>? = null

        /**
         * @see [EnableMethodPayload.pluginName]
         */
        public var pluginName: String? = null

        /**
         * @see [EnableMethodPayload.pluginVersion]
         */
        public var pluginVersion: String? = null

        /**
         * @see [EnableMethodPayload.sealWrap]
         */
        public var sealWrap: Boolean? = null

        /**
         * Build the instance of [EnableMethodPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): EnableMethodPayload {
            return EnableMethodPayload(
                type = type,
                config = configBuilder?.let { Config().apply(it) },
                description = description,
                externalEntropyAccess = externalEntropyAccess,
                local = local,
                options = options,
                pluginName = pluginName,
                pluginVersion = pluginVersion,
                sealWrap = sealWrap
            )
        }

        /**
         * Sets the config builder.
         *
         * @param builder Builder to create [Config] instance.
         */
        public fun config(builder: BuilderDsl<Config>) {
            configBuilder = builder
        }
    }
}
