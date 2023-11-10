package com.github.hansanto.kault.system.auth.response

import com.github.hansanto.kault.system.auth.common.ListingVisibility
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AuthReadTuningInformationResponse(

    @SerialName("allowed_managed_keys")
    public val allowedManagedKeys: List<String>?,

    @SerialName("allowed_response_headers")
    public val allowedResponseHeaders: List<String>?,

    @SerialName("audit_non_hmac_request_keys")
    public val auditNonHmacRequestKeys: List<String>?,

    @SerialName("audit_non_hmac_response_keys")
    public val auditNonHmacResponseKeys: List<String>?,

    @SerialName("passthrough_request_headers")
    public val passthroughRequestHeaders: List<String>?,

    @SerialName("default_lease_ttl")
    public val defaultLeaseTTL: Long,

    @SerialName("description")
    public val description: String,

    @SerialName("external_entropy_access")
    public val externalEntropyAccess: Boolean?,

    @SerialName("force_no_cache")
    public val forceNoCache: Boolean,

    @SerialName("listing_visibility")
    public val listingVisibility: ListingVisibility?,

    @SerialName("max_lease_ttl")
    public val maxLeaseTTL: Long,

    @SerialName("plugin_version")
    public val pluginVersion: String?,

    @SerialName("token_type")
    public val tokenType: String,

    @SerialName("user_lockout_counter_reset_duration")
    public val userLockoutCounterResetDuration: Long?,

    @SerialName("user_lockout_disable")
    public val userLockoutDisable: Boolean?,

    @SerialName("user_lockout_duration")
    public val userLockoutDuration: Long?,

    @SerialName("user_lockout_threshold")
    public val userLockoutThreshold: Long?
)
