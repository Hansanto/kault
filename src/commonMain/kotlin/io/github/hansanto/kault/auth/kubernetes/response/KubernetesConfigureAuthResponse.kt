package io.github.hansanto.kault.auth.kubernetes.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class KubernetesConfigureAuthResponse(

    /**
     * Host string, a host:port pair, or a URL to the base of the Kubernetes API server.
     */
    @SerialName("kubernetes_host")
    public val kubernetesHost: String,

    /**
     * PEM encoded CA cert for use by the TLS client used to talk with the API.
     */
    @SerialName("kubernetes_ca_cert")
    public val kubernetesCaCert: String,

    /**
     * List of PEM-formatted public keys or certificates
     * used to verify the signatures of kubernetes service account JWTs.
     * If a certificate is given, its public key will be extracted.
     * Not every installation of Kubernetes exposes these keys.
     */
    @SerialName("pem_keys")
    public val pemKeys: List<String>,

    /**
     * Disable defaulting to the local CA cert and service account JWT when running in a Kubernetes pod.
     */
    @SerialName("disable_local_ca_jwt")
    public val disableLocalCaJwt: Boolean

)
