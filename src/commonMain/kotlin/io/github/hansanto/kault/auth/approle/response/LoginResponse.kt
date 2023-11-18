package io.github.hansanto.kault.auth.approle.response

import io.github.hansanto.kault.auth.approle.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class LoginResponse(

    /**
     * Client token to interact with Vault.
     */
    @SerialName("client_token")
    public val clientToken: String,

    /**
     * Accessor of the client token.
     */
    @SerialName("accessor")
    public val accessor: String,

    /**
     * List of token policies to encode onto generated tokens. Depending on the auth method, this list may be supplemented by user/group/other values.
     */
    @SerialName("token_policies")
    public val tokenPolicies: List<String>,

    /**
     * Metadata of the client token.
     */
    @SerialName("metadata")
    public val metadata: Map<String, String>,

    /**
     * Duration in seconds before the token expires.
     */
    @SerialName("lease_duration")
    public val leaseDuration: VaultDuration,

    /**
     * Whether the token is renewable.
     */
    @SerialName("renewable")
    public val renewable: Boolean,

    /**
     * Entity ID associated with the token.
     */
    @SerialName("entity_id")
    public val entityId: String,

    /**
     * The type of token that should be generated. Can be service, batch, or default to use the mount's tuned default (which unless changed will be service tokens). For token store roles, there are two additional possibilities: default-service and default-batch which specify the type to return unless the client requests a different type at generation time.
     */
    @SerialName("token_type")
    public val tokenType: TokenType,

    /**
     * If true, tokens created against this policy will be orphan tokens (they will have no parent). As such, they will not be automatically revoked by the revocation of any other token.
     */
    @SerialName("orphan")
    public val orphan: Boolean,

    /**
     * The maximum uses for the given token. This can be used to create a one-time-token or limited use token. The value of 0 has no limit to the number of uses.
     */
    @SerialName("num_uses")
    public val numUses: Long
)
