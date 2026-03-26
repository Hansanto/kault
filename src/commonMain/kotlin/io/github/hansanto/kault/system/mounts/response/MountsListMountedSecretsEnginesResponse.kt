package io.github.hansanto.kault.system.mounts.response

import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

public typealias MountsListMountedSecretsEnginesResponse = Map<String, MountsListMountedSecretsEnginesResponseEntry>

@Serializable
public data class MountsListMountedSecretsEnginesResponseEntry(
    /**
     * Name of the accessor.
     */
    @SerialName("accessor")
    val accessor: String,

    /**
     * Specifies configuration options for this mount.
     */
    @SerialName("config")
    val config: Config,

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
    val uuid: String,
) {

    @Serializable
    public data class Config(
        /**
         * The default lease duration.
         */
        @SerialName("default_lease_ttl")
        public val defaultLeaseTtl: VaultDuration,

        /**
         * Disable caching.
         */
        @SerialName("force_no_cache")
        public val forceNoCache: Boolean,

        /**
         * The maximum lease duration.
         */
        @SerialName("max_lease_ttl")
        public val maxLeaseTtl: VaultDuration,
    )
}
