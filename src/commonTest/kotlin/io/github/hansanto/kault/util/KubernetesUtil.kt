package io.github.hansanto.kault.util

object KubernetesUtil {
    val host: String = getKubernetesHost()
    val token: String = getKubernetesToken()
    val caCert: String = getKubernetesCaCert()
}

/**
 * Environment variable name to retrieve IP of local kubernetes.
 */
private val KUBERNETES_IP_ENV = EnvValue("KUBERNETES_IP") { "192.168.49.2" }

/**
 * Environment variable name to retrieve port of local kubernetes.
 * For the default port value, check [documentation](https://minikube.sigs.k8s.io/docs/commands/start/).
 */
private val KUBERNETES_PORT_ENV = EnvValue("KUBERNETES_PORT") { "8443" }

/**
 * Environment variable name to retrieve token of local kubernetes.
 */
private val KUBERNETES_TOKEN_ENV = EnvValue("KUBERNETES_TOKEN") {
    // The token file is defined by the setup script in configuration/kubernetes
    ResourceValue("configuration/kubernetes/token.tmp").value
}

/**
 * File variable to retrieve CA certificate of local kubernetes.
 */
private val KUBERNETES_CA_CERT_FILE = SystemFileValue(".minikube/ca.crt")

/**
 * Get the kubernetes host URL.
 * @return URL of kubernetes host.
 */
private fun getKubernetesHost(): String {
    return "https://${getKubernetesIp()}:${getKubernetesPort()}"
}

/**
 * Get the IP of kubernetes from the environment variable.
 * @return IP of kubernetes.
 */
private fun getKubernetesIp(): String = KUBERNETES_IP_ENV.value

/**
 * Get the kubernetes port from the environment variable.
 * @return Port of kubernetes.
 */
private fun getKubernetesPort(): String = KUBERNETES_PORT_ENV.value

/**
 * Get the token of kubernetes from the environment variable [KUBERNETES_TOKEN_ENV].
 * @return Token of kubernetes.
 */
private fun getKubernetesToken(): String = KUBERNETES_TOKEN_ENV.value

/**
 * Get the CA certificate of kubernetes.
 * @return CA certificate of kubernetes in string format.
 */
private fun getKubernetesCaCert(): String = KUBERNETES_CA_CERT_FILE.value
