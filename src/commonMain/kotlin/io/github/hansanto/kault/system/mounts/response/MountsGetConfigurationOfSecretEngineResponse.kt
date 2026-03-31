package io.github.hansanto.kault.system.mounts.response

import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.system.common.ListingVisibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MountsGetConfigurationOfSecretEngineResponse(

    @SerialName("accessor")
    val accessor: String,

    /**
     * Specifies configuration options for this mount; if set on a specific mount, values will override any global defaults (e.g. the system TTL/Max TTL).
     */
    @SerialName("config")
    val config: Config,

    @SerialName("deprecation_status")
    val deprecationStatus: String?,

    /**
     * Specifies the human-friendly description of the mount.
     */
    @SerialName("description")
    val description: String,

    /**
     * Enable the secrets engine to access Vault's external entropy source.
     */
    @SerialName("external_entropy_access")
    val externalEntropyAccess: Boolean,

    /**
     * Specifies if the secrets engine is a local mount only. Local mounts are not replicated nor (if a secondary) removed by replication.
     */
    @SerialName("local")
    val local: Boolean,

    /**
     * Specifies mount type specific options that are passed to the backend.
     */
    @SerialName("options")
    val options: Map<String, String>?,

    /**
     * Specifies the semantic version of the plugin to use, e.g. "v1.0.0". If unspecified, the server will select any matching unversioned plugin that may have been registered, the latest versioned plugin registered, or a built-in plugin in that order of precedence.
     */
    @SerialName("plugin_version")
    val pluginVersion: String,

    @SerialName("running_plugin_version")
    val runningPluginVersion: String,

    @SerialName("running_sha256")
    val runningSha256: String,

    /**
     * Enable seal wrapping for the mount, causing values stored by the mount to be wrapped by the seal's encryption capability.
     */
    @SerialName("seal_wrap")
    val sealWrap: Boolean,

    /**
     * Specifies the type of the backend, such as "aws".
     */
    @SerialName("type")
    val type: String,

    /**
     * ID of the entry.
     */
    @SerialName("uuid")
    val uuid: String
) {

    @Serializable
    public data class Config(
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
    )
}
