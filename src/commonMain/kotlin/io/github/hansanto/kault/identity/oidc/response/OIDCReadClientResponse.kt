package io.github.hansanto.kault.identity.oidc.response

import io.github.hansanto.kault.identity.oidc.common.ClientType
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCReadClientResponse(
    /**
     * A reference to a [named key](https://developer.hashicorp.com/vault/api-docs/secret/identity/tokens#create-a-named-key) resource. This key will be used to sign ID tokens for the client. This cannot be modified after creation. If not supplied, defaults to the built-in [default key](https://developer.hashicorp.com/vault/docs/concepts/oidc-provider#keys).
     */
    @SerialName("key")
    public var key: String,

    /**
     * Redirection URI values used by the client. One of these values must exactly match the redirect_uri parameter value used in each [authentication request](https://openid.net/specs/openid-connect-core-1_0.html#AuthRequest).
     */
    @SerialName("redirect_uris")
    public var redirectUris: List<String>,

    /**
     * A list of assignment resources associated with the client. Client assignments limit the Vault entities and groups that are allowed to authenticate through the client. By default, no Vault entities are allowed. To allow all Vault entities to authenticate through the client, supply the built-in [allow_all](https://developer.hashicorp.com/vault/docs/concepts/oidc-provider#assignments) assignment.
     */
    @SerialName("assignments")
    public var assignments: List<String>,

    /**
     * ID of the client.
     */
    @SerialName("client_id")
    public var clientId: String,

    /**
     * Secret of the client. Only present for [ClientType.CONFIDENTIAL] clients.
     */
    @SerialName("client_secret")
    public var clientSecret: String? = null,

    /**
     * The [client type](https://datatracker.ietf.org/doc/html/rfc6749#section-2.1) based on its ability to maintain confidentiality of credentials. This cannot be modified after creation. The following list details the differences between confidential and public clients in Vault:
     * - [ClientType.CONFIDENTIAL]:
     *      - Capable of maintaining the confidentiality of its credentials
     *      - Has a client secret
     *      - Uses the client_secret_basic or client_secret_post [client authentication method](https://openid.net/specs/openid-connect-core-1_0.html#ClientAuthentication)
     *      - May use Proof Key for Code Exchange ([PKCE](https://datatracker.ietf.org/doc/html/rfc7636)) for the authorization code flow
     *
     * - [ClientType.PUBLIC]:
     *     - Not capable of maintaining the confidentiality of its credentials
     *     - Does not have a client secret
     *     - Uses the none [client authentication method](https://openid.net/specs/openid-connect-core-1_0.html#ClientAuthentication)
     *     - Must use Proof Key for Code Exchange ([PKCE](https://datatracker.ietf.org/doc/html/rfc7636)) for the authorization code flow
     */
    @SerialName("client_type")
    public var clientType: ClientType,

    /**
     * The time-to-live for ID tokens obtained by the client. Accepts [duration format strings](https://developer.hashicorp.com/vault/docs/concepts/duration-format). The value should be less than the verification_ttl on the key.
     */
    @SerialName("id_token_ttl")
    public var idTokenTTL: VaultDuration,

    /**
     * The time-to-live for access tokens obtained by the client. Accepts [duration format strings](https://developer.hashicorp.com/vault/docs/concepts/duration-format).
     */
    @SerialName("access_token_ttl")
    public var accessTokenTTL: VaultDuration
)
