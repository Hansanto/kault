package io.github.hansanto.kault.auth.token.payload

import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TokenWriteRolePayload(
    /**
     * If set, tokens can be created with any subset of the policies in this list, rather than the normal semantics of tokens being a subset of the calling token's policies. The parameter is a comma-delimited string of policy names. If at creation time no_default_policy is not set and "default" is not contained in disallowed_policies or glob matched in disallowed_policies_glob, the "default" policy will be added to the created token automatically.
     */
    @SerialName("allowed_policies")
    public var allowedPolicies: List<String>? = null,

    /**
     * If set, successful token creation via this role will require that no policies in the given list are requested. The parameter is a comma-delimited string of policy names. Adding "default" to this list will prevent "default" from being added automatically to created tokens.
     */
    @SerialName("disallowed_policies")
    public var disallowedPolicies: List<String>? = null,

    /**
     * If set, tokens can be created with any subset of glob matched policies in this list, rather than the normal semantics of tokens being a subset of the calling token's policies. The parameter is a comma-delimited string of policy name globs. If at creation time no_default_policy is not set and "default" is not contained in disallowed_policies or glob matched in disallowed_policies_glob, the "default" policy will be added to the created token automatically. If combined with allowed_policies policies need to only match one of the two lists to be permitted. Note that unlike allowed_policies the policies listed in allowed_policies_glob will not be added to the token when no policies are specified in the call to /auth/token/create/:role_name.
     */
    @SerialName("allowed_policies_glob")
    public var allowedPoliciesGlob: List<String>? = null,

    /**
     * If set, successful token creation via this role will require that no requested policies glob match any of policies in this list. The parameter is a comma-delimited string of policy name globs. Adding any glob that matches "default" to this list will prevent "default" from being added automatically to created tokens. If combined with disallowed_policies policies need to only match one of the two lists to be blocked.
     */
    @SerialName("disallowed_policies_glob")
    public var disallowedPoliciesGlob: List<String>? = null,

    /**
     * If true, tokens created against this policy will be orphan tokens (they will have no parent). As such, they will not be automatically revoked by the revocation of any other token.
     */
    @SerialName("orphan")
    public var orphan: Boolean? = null,

    /**
     * Set to false to disable the ability of the token to be renewed past its initial TTL. Setting the value to true will allow the token to be renewable up to the system/mount maximum TTL.
     */
    @SerialName("renewable")
    public var renewable: Boolean? = null,

    /**
     * If set, tokens created against this role will have the given suffix as part of their path in addition to the role name. This can be useful in certain scenarios, such as keeping the same role name in the future but revoking all tokens created against it before some point in time. The suffix can be changed, allowing new callers to have the new suffix as part of their path, and then tokens with the old suffix can be revoked via /sys/leases/revoke-prefix.
     */
    @SerialName("path_suffix")
    public var pathSuffix: String? = null,

    /**
     * String or JSON list of allowed entity aliases. If set, specifies the entity aliases which are allowed to be used during token generation. This field supports globbing. Note that allowed_entity_aliases is not case sensitive.
     */
    @SerialName("allowed_entity_aliases")
    public var allowedEntityAliases: List<String>? = null,

    /**
     * List of CIDR blocks; if set, specifies blocks of IP addresses which can authenticate successfully, and ties the resulting token to these blocks as well.
     */
    @SerialName("token_bound_cidrs")
    public var tokenBoundCidrs: List<String>? = null,

    /**
     * If set, will encode an explicit max TTL onto the token. This is a hard cap even if token_ttl and token_max_ttl would otherwise allow a renewal.
     */
    @SerialName("token_explicit_max_ttl")
    public var tokenExplicitMaxTTL: VaultDuration? = null,

    /**
     * If set, the default policy will not be set on generated tokens; otherwise it will be added to the policies set in token_policies.
     */
    @SerialName("token_no_default_policy")
    public var tokenNoDefaultPolicy: Boolean? = null,

    /**
     * The maximum number of times a generated token may be used (within its lifetime); 0 means unlimited. If you require the token to have the ability to create child tokens, you will need to set this value to 0.
     */
    @SerialName("token_num_uses")
    public var tokenNumUses: Long? = null,

    /**
     * The maximum allowed period value when a periodic token is requested from this role.
     */
    @SerialName("token_period")
    public var tokenPeriod: VaultDuration? = null,

    /**
     * The type of token that should be generated. Can be service, batch, or default to use the mount's tuned default (which unless changed will be service tokens). For token store roles, there are two additional possibilities: default-service and default-batch which specify the type to return unless the client requests a different type at generation time.
     */
    @SerialName("token_type")
    public var tokenType: TokenType? = null
)
