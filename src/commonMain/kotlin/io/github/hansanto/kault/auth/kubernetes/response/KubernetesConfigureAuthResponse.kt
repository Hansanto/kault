package io.github.hansanto.kault.auth.kubernetes.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class KubernetesConfigureAuthResponse(

    /**
     * Host must be a host string, a host:port pair, or a URL to the base of the Kubernetes API server.
     */
    @SerialName("kubernetes_host")
    public var kubernetesHost: String,

    /**
     * PEM encoded CA cert for use by the TLS client used to talk with the API.
     */
    @SerialName("kubernetes_ca_cert")
    public var kubernetesCaCert: String,

    /**
     * List of PEM-formated public keys or certificates used to verify the signatures of kubernetes service account JWTs. If a certificate is given, its public key will be extracted. Not every installation of Kubernetes exposes these keys.
     */
    @SerialName("pem_keys")
    public var pemKeys: List<String>,

    /**
     * Disable defaulting to the local CA cert and service account JWT when running in a Kubernetes pod.
     */
    @SerialName("disable_local_ca_jwt")
    public var disableLocalCaJwt: Boolean

)
