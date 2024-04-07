package io.github.hansanto.kault.auth.token.payload

import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TokenCreatePayload(

    /**
     * The ID of the client token. Can only be specified by a root token. The ID provided may not contain a . character. Otherwise, the token ID is a randomly generated value.
     *
     * Note: The ID should not start with the s. prefix.
     */
    @SerialName("id")
    public var id: String? = null,

    /**
     * The name of the token role.
     */
    @SerialName("role_name")
    public var roleName: String? = null,

    /**
     * A list of policies for the token. This must be a subset of the policies belonging to the token making the request, unless the calling token is root or contains sudo capabilities to auth/token/create. If not specified, defaults to all the policies of the calling token.
     */
    @SerialName("policies")
    public var policies: List<String>? = null,

    /**
     * A map of string to string valued metadata. This is passed through to the audit devices.
     */
    @SerialName("meta")
    public var metadata: Map<String, String>? = null,

    /**
     * If true the default policy will not be contained in this token's policy set.
     */
    @SerialName("no_parent")
    public var noParent: Boolean? = null,

    /**
     * If true the default policy will not be contained in this token's policy set.
     */
    @SerialName("no_default_policy")
    public var noDefaultPolicy: Boolean? = null,

    /**
     * Set to false to disable the ability of the token to be renewed past its initial TTL. Setting the value to true will allow the token to be renewable up to the system/mount maximum TTL.
     */
    @SerialName("renewable")
    public var renewable: Boolean? = null,

    /**
     * The TTL period of the token, provided as "1h", where hour is the largest suffix. If not provided, the token is valid for the default lease TTL, or indefinitely if the root policy is used.
     */
    @SerialName("ttl")
    public var ttl: VaultDuration? = null,

    /**
     * The token type. Can be "batch" or "service". Defaults to the type specified by the role configuration named by role_name.
     */
    @SerialName("type")
    public var type: TokenType? = null,

    /**
     * If set, the token will have an explicit max TTL set upon it. This maximum token TTL cannot be changed later, and unlike with normal tokens, updates to the system/mount max TTL value will have no effect at renewal time -- the token will never be able to be renewed or used past the value set at issue time.
     */
    @SerialName("explicit_max_ttl")
    public var explicitMaxTTL: VaultDuration? = null,

    /**
     * Name to associate with this token
     */
    @SerialName("display_name")
    public var displayName: String? = null,

    /**
     * The maximum uses for the given token. This can be used to create a one-time-token or limited use token. The value of 0 has no limit to the number of uses.
     */
    @SerialName("num_uses")
    public var numUses: Long? = null,

    /**
     * If specified, the token will be periodic; it will have no maximum TTL (unless an "explicit-max-ttl" is also set) but every renewal will use the given period. Requires a root token or one with the sudo capability.
     */
    @SerialName("period")
    public var period: VaultDuration? = null,

    /**
     * Name of the entity alias to associate with during token creation. Only works in combination with role_name argument and used entity alias must be listed in allowed_entity_aliases. If this has been specified, the entity will not be inherited from the parent.
     */
    @SerialName("entity_alias")
    public var entityAlias: String? = null
)
