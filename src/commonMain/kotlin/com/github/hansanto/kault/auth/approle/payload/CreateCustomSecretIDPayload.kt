package com.github.hansanto.kault.auth.approle.payload

import com.github.hansanto.kault.extension.toJsonString
import com.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.serializer

@Serializable
public data class CreateCustomSecretIDPayload(

    /**
     * SecretID to be attached to the Role.
     */
    @SerialName("secret_id")
    public var secretId: String,

    /**
     * Metadata to be tied to the SecretID. This should be a JSON-formatted string containing the metadata in key-value pairs. This metadata will be set on tokens issued with this SecretID, and is logged in audit logs in plaintext.
     */
    @SerialName("metadata")
    public var metadata: String? = null,

    /**
     * List of CIDR blocks enforcing secret IDs to be used from specific set of IP addresses. If secret_id_bound_cidrs is set on the role, then the list of CIDR blocks listed here should be a subset of the CIDR blocks listed on the role.
     */
    @SerialName("cidr_list")
    public var cidrList: List<String>? = null,

    /**
     * List of CIDR blocks; if set, specifies blocks of IP addresses which can use the auth tokens generated by this SecretID. Overrides any role-set value but must be a subset.
     */
    @SerialName("token_bound_cidrs")
    public var tokenBoundCidrs: List<String>? = null,

    /**
     * Number of times this SecretID can be used, after which the SecretID expires. A value of zero will allow unlimited uses. Overrides secret_id_num_uses role option when supplied. May not be higher than role's secret_id_num_uses.
     */
    @SerialName("num_uses")
    public var numUses: Long? = null,

    /**
     * Duration in seconds (3600) or an integer time unit (60m) after which this SecretID expires. A value of zero will allow the SecretID to not expire. Overrides secret_id_ttl role option when supplied. May not be longer than role's secret_id_ttl.
     */
    @SerialName("ttl")
    public var ttl: VaultDuration? = null
) {

    /**
     * Builder class to simplify the creation of [CreateCustomSecretIDPayload].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder {

        /**
         * @see [CreateCustomSecretIDPayload.secretId]
         */
        public lateinit var secretId: String

        /**
         * @see [CreateCustomSecretIDPayload.metadata]
         */
        public var metadata: String? = null

        /**
         * @see [CreateCustomSecretIDPayload.cidrList]
         */
        public var cidrList: List<String>? = null

        /**
         * @see [CreateCustomSecretIDPayload.tokenBoundCidrs]
         */
        public var tokenBoundCidrs: List<String>? = null

        /**
         * @see [CreateCustomSecretIDPayload.numUses]
         */
        public var numUses: Long? = null

        /**
         * @see [CreateCustomSecretIDPayload.ttl]
         */
        public var ttl: VaultDuration? = null

        /**
         * Build the instance of [CreateCustomSecretIDPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): CreateCustomSecretIDPayload = CreateCustomSecretIDPayload(
            secretId = secretId,
            metadata = metadata,
            cidrList = cidrList,
            tokenBoundCidrs = tokenBoundCidrs,
            numUses = numUses,
            ttl = ttl
        )

        /**
         * Sets the metadata field from a map and converts it to a JSON string.
         *
         * @param metadata A map containing key-value pairs of metadata.
         */
        public fun metadata(metadata: Map<String, String>) {
            this.metadata = metadata.toJsonString(String.serializer(), String.serializer())
        }
    }

    /**
     * Sets the metadata field from a map and converts it to a JSON string.
     *
     * @param metadata A map containing key-value pairs of metadata.
     */
    public fun metadata(metadata: Map<String, String>) {
        this.metadata = metadata.toJsonString(String.serializer(), String.serializer())
    }
}
