package io.github.hansanto.kault.identity.oidc.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCReadProviderPublicKeysResponse(
    /**
     * The list of keys.
     */
    val keys: List<JWK>,
) {

    /**
     * Type corresponding to [RFC7517](https://www.rfc-editor.org/rfc/rfc7517.html).
     */
    @Serializable
    public data class JWK(
        /**
         * Public Key Use.
         * Identifies the intended use of the public key.
         * The "use" parameter is employed to indicate whether a public key is used for encrypting data or verifying the signature on data.
         * Values defined by this specification are:
         * - "sig" (signature)
         * - "enc" (encryption)
         */
        @SerialName("use")
        val use: String,

        /**
         * Key Type.
         * Identifies the cryptographic algorithm family used with the key, such as "RSA" or "EC".
         */
        @SerialName("kty")
        val kty: String,

        /**
         * Key ID.
         * Used to match a specific key.
         */
        @SerialName("kid")
        val kid: String,

        /**
         * Algorithm.
         * Identifies the algorithm intended for use with the key
         */
        @SerialName("alg")
        val alg: String,

        /**
         * Modulus.
         */
        @SerialName("n")
        val n: String,

        /**
         * Exponent.
         */
        @SerialName("e")
        val e: String,

        /**
         * Key Operations.
         * Identifies the operation(s) for which the key is intended to be used.
         */
        @SerialName("key_ops")
        val keyOps: List<String>? = null,

        /**
         * X.509 URL.
         * Is a URI [RFC3986](https://www.rfc-editor.org/rfc/rfc3986) that refers to a resource for an X.509 public key certificate or certificate chain [RFC5280](https://www.rfc-editor.org/rfc/rfc5280).
         */
        @SerialName("x5u")
        val x5u: String? = null,

        /**
         * X.509 certificate chain.
         * Contains a chain of one or more PKIX certificates [RFC5280](https://www.rfc-editor.org/rfc/rfc5280).
         */
        @SerialName("x5c")
        val x5c: List<String>? = null,

        /**
         * X.509 certificate SHA-1 thumbprint.
         * Is a base64url-encoded SHA-1 thumbprint (a.k.a. digest) of the DER encoding of an X.509 certificate [RFC5280](https://www.rfc-editor.org/rfc/rfc5280).
         */
        @SerialName("x5t")
        val x5t: String? = null,

        /**
         * X.509 Certificate SHA-256 Thumbprint.
         * Is a base64url-encoded SHA-256 thumbprint (a.k.a. digest) of the DER encoding of an X.509 certificate [RFC5280](https://www.rfc-editor.org/rfc/rfc5280).
         */
        @SerialName("x5t#S256")
        val x5tS256: String? = null,
    )
}
