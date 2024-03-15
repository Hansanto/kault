package io.github.hansanto.kault.auth.kubernetes.payload

import io.github.hansanto.kault.KaultDsl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class KubernetesConfigureAuthPayload(

    /**
     * Host must be a host string, a host:port pair, or a URL to the base of the Kubernetes API server.
     */
    @SerialName("kubernetes_host")
    public var kubernetesHost: String,

    /**
     * PEM encoded CA cert for use by the TLS client used to talk with the API.
     */
    @SerialName("kubernetes_ca_cert")
    public var kubernetesCaCert: String? = null,

    /**
     * Optional list of PEM-formatted public keys or certificates
     * used to verify the signatures of kubernetes service account JWTs.
     * If a certificate is given, its public key will be extracted.
     * Not every installation of Kubernetes exposes these keys.
     */
    @SerialName("pem_keys")
    public var pemKeys: List<String>? = null,

    /**
     * Disable defaulting to the local CA cert and service account JWT when running in a Kubernetes pod.
     */
    @SerialName("disable_local_ca_jwt")
    public var disableLocalCaJwt: Boolean? = null,

    /**
     * A service account JWT (or other token) used as a bearer token to access the TokenReview API to validate other JWTs during login. If not set the JWT used for login will be used to access the API.
     */
    @SerialName("token_reviewer_jwt")
    public var tokenReviewerJwt: String? = null
) {

    /**
     * Builder class to simplify the creation of [KubernetesConfigureAuthPayload].
     */
    @KaultDsl
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder {

        /**
         * @see [KubernetesConfigureAuthPayload.kubernetesHost]
         */
        public lateinit var kubernetesHost: String

        /**
         * @see [KubernetesConfigureAuthPayload.disableLocalCaJwt]
         */
        public var disableLocalCaJwt: Boolean? = null

        /**
         * @see [KubernetesConfigureAuthPayload.kubernetesCaCert]
         */
        public var kubernetesCaCert: String? = null

        /**
         * @see [KubernetesConfigureAuthPayload.pemKeys]
         */
        public var pemKeys: List<String>? = null

        /**
         * @see [KubernetesConfigureAuthPayload.tokenReviewerJwt]
         */
        public var tokenReviewerJwt: String? = null

        /**
         * Build the instance of [KubernetesConfigureAuthPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): KubernetesConfigureAuthPayload = KubernetesConfigureAuthPayload(
            kubernetesHost = kubernetesHost,
            disableLocalCaJwt = disableLocalCaJwt,
            kubernetesCaCert = kubernetesCaCert,
            pemKeys = pemKeys,
            tokenReviewerJwt = tokenReviewerJwt
        )
    }
}
