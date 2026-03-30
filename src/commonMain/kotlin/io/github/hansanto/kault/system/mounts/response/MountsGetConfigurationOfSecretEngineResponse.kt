package io.github.hansanto.kault.system.mounts.response

import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.system.common.ListingVisibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class MountsGetConfigurationOfSecretEngineResponse(

    @SerialName("accessor")
    val accessor: String,

    @SerialName("config")
    val config: Config,

    @SerialName("deprecation_status")
    val deprecationStatus: String?,

    @SerialName("description")
    val description: String,

    @SerialName("external_entropy_access")
    val externalEntropyAccess: Boolean,

    @SerialName("local")
    val local: Boolean,

    @SerialName("options")
    val options: Map<String, String>?,

    @SerialName("plugin_version")
    val pluginVersion: String,

    @SerialName("running_plugin_version")
    val runningPluginVersion: String,

    @SerialName("running_sha256")
    val runningSha256: String,

    @SerialName("seal_wrap")
    val sealWrap: Boolean,

    @SerialName("type")
    val type: String,

    @SerialName("uuid")
    val uuid: String
) {

    @Serializable
    public data class Config(
        @SerialName("default_lease_ttl")
        val defaultLeaseTTL: VaultDuration,

        @SerialName("max_lease_ttl")
        val maxLeaseTTL: VaultDuration,

        @SerialName("force_no_cache")
        val forceNoCache: Boolean,

        @SerialName("audit_non_hmac_request_keys")
        val auditNonHmacRequestKeys: List<String>?,

        @SerialName("audit_non_hmac_response_keys")
        val auditNonHmacResponseKeys: List<String>?,

        @SerialName("listing_visibility")
        val listingVisibility: ListingVisibility?,

        @SerialName("passthrough_request_headers")
        val passthroughRequestHeaders: List<String>?,

        @SerialName("allowed_response_headers")
        val allowedResponseHeaders: List<String>?,

        @SerialName("allowed_managed_keys")
        val allowedManagedKeys: List<String>?,
    )
}
