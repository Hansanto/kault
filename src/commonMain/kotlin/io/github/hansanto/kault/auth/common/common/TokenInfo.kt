package io.github.hansanto.kault.auth.common.common

import kotlinx.datetime.Instant

public data class TokenInfo(
    /**
     * Token to interact with Vault.
     */
    public val token: String,

    /**
     * Accessor of the client token.
     */
    public val accessor: String? = null,

    /**
     * List of token policies to encode onto generated tokens.
     * Depending on the auth method, this list may be supplemented by user/group/other values.
     */
    public val tokenPolicies: List<String> = emptyList(),

    /**
     * Metadata of the client token.
     */
    public val metadata: Map<String, String> = emptyMap(),

    /**
     * Date where the token will expire.
     */
    public val expirationDate: Instant? = null,

    /**
     * Whether the token is renewable.
     */
    public val renewable: Boolean = false,

    /**
     * Entity ID associated with the token.
     */
    public val entityId: String = "",

    /**
     * The type of token that should be generated. Can be service, batch, or default to use the mount's tuned default (which unless changed will be service tokens).
     * For token store roles, there are two additional possibilities: default-service and default-batch which specify the type to return unless the client requests a different type at generation time.
     */
    public val tokenType: TokenType = TokenType.DEFAULT,

    /**
     * If true, tokens created against this policy will be orphan tokens (they will have no parent).
     * As such, they will not be automatically revoked by the revocation of any other token.
     */
    public val orphan: Boolean = true,

    /**
     * The maximum uses for the given token. This can be used to create a one-time-token or limited use token.
     * The value of 0 has no limit to the number of uses.
     */
    public val numUses: Long = 0
)
