package io.github.hansanto.kault.system.mounts.payload

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.system.common.ListingVisibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MountsEnableSecretsEnginePayload(
    /**
     * Specifies the type of the backend, such as "aws".
     */
    @SerialName("type")
    public var type: String,

    /**
     * Specifies the human-friendly description of the mount.
     */
    @SerialName("description")
    public var description: String? = null,

    /**
     * Specifies configuration options for this mount; if set on a specific mount, values will override any global defaults (e.g. the system TTL/Max TTL).
     */
    @SerialName("config")
    public var config: Config? = null,

    /**
     * Specifies mount type specific options that are passed to the backend.
     */
    @SerialName("options")
    public var options: Map<String, String>? = null,

    /**
     * Specifies if the secrets engine is a local mount only. Local mounts are not replicated nor (if a secondary) removed by replication.
     */
    @SerialName("local")
    public var local: Boolean? = null,

    /**
     * Enable seal wrapping for the mount, causing values stored by the mount to be wrapped by the seal's encryption capability.
     */
    @SerialName("seal_wrap")
    public var sealWrap: Boolean? = null,

    /**
     * Enable the secrets engine to access Vault's external entropy source.
     */
    @SerialName("external_entropy_access")
    public var externalEntropyAccess: Boolean? = null,
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
         * Disable caching.
         */
        @SerialName("force_no_cache")
        public var forceNoCache: Boolean? = null,

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
         * Specifies the semantic version of the plugin to use, e.g. "v1.0.0". If unspecified, the server will select any matching unversioned plugin that may have been registered, the latest versioned plugin registered, or a built-in plugin in that order of precedence.
         */
        @SerialName("plugin_version")
        public var pluginVersion: String? = null,

        /**
         * List of managed key registry entry names that the mount in question is allowed to access.
         */
        @SerialName("allowed_managed_keys")
        public var allowedManagedKeys: List<String>? = null,

        /**
         * List of allowed authentication mount accessors the backend can request delegated authentication for.
         */
        @SerialName("delegated_auth_accessors")
        public var delegatedAuthAccessors: List<String>? = null,

        /**
         * The key to use for signing plugin workload identity tokens. If not provided, this will default to Vault's OIDC [default key](https://developer.hashicorp.com/vault/docs/concepts/oidc-provider#keys).
         */
        @SerialName("identity_token_key")
        public var identityTokenKey: String? = null
    )

    /**
     * Builder class to simplify the creation of [MountsEnableSecretsEnginePayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [MountsEnableSecretsEnginePayload.type]
         */
        public lateinit var type: String

        /**
         * @see [MountsEnableSecretsEnginePayload.description]
         */
        public var description: String? = null

        /**
         * Builder to define the [Config] of [MountsEnableSecretsEnginePayload].
         */
        private var configBuilder: BuilderDsl<Config>? = null

        /**
         * @see [MountsEnableSecretsEnginePayload.options]
         */
        public var options: Map<String, String>? = null

        /**
         * @see [MountsEnableSecretsEnginePayload.local]
         */
        public var local: Boolean? = null

        /**
         * @see [MountsEnableSecretsEnginePayload.sealWrap]
         */
        public var sealWrap: Boolean? = null

        /**
         * @see [MountsEnableSecretsEnginePayload.externalEntropyAccess]
         */
        public var externalEntropyAccess: Boolean? = null

        /**
         * Build the instance of [MountsEnableSecretsEnginePayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): MountsEnableSecretsEnginePayload = MountsEnableSecretsEnginePayload(
            type = type,
            description = description,
            config = configBuilder?.let { Config().apply(it) },
            options = options,
            local = local,
            sealWrap = sealWrap,
            externalEntropyAccess = externalEntropyAccess
        )

        /**
         * Sets the config builder.
         *
         * @param builder Builder to create [Config] instance.
         */
        public fun config(builder: BuilderDsl<Config>) {
            configBuilder = builder
        }

        /**
         * Sets the options for [VaultSecretEngine][io.github.hansanto.kault.engine.VaultSecretEngine].
         *
         * @param version The version of the key-value secrets engine to enable, e.g. 1 or 2.
         */
        public fun kvOptions(version: Int) {
            options = mapOf("version" to version.toString())
        }
    }
}
