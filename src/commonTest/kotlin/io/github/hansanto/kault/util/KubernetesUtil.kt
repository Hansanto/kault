package io.github.hansanto.kault.util

import io.kotest.mpp.syspropOrEnv
import kotlinx.io.files.Path

/**
 * Environment variable name to retrieve IP of local kubernetes.
 */
private const val KUBERNETES_IP_ENV = "KUBERNETES_IP"

/**
 * Environment variable name to retrieve port of local kubernetes.
 */
private const val KUBERNETES_PORT_ENV = "KUBERNETES_PORT"

/**
 * Environment variable name to retrieve token of local kubernetes.
 */
private const val KUBERNETES_TOKEN_ENV = "KUBERNETES_TOKEN"

/**
 * Default port of kubernetes, check [documentation](https://minikube.sigs.k8s.io/docs/commands/start/).
 */
private const val KUBERNETES_DEFAULT_PORT = 8443

/**
 * Folder name of kubernetes in user's home directory.
 */
private const val MINIKUBE_FOLDER = ".minikube"

/**
 * File name of CA certificate in kubernetes folder.
 */
private const val CA_CERT_FILE = "ca.crt"

/**
 * Get the kubernetes host URL.
 * @param port Port of kubernetes, default is [KUBERNETES_DEFAULT_PORT].
 * @return URL of kubernetes host.
 */
fun getKubernetesHost(port: Int = syspropOrEnv(KUBERNETES_PORT_ENV)?.toInt() ?: KUBERNETES_DEFAULT_PORT): String {
    return "https://${getKubernetesIp()}:$port"
}

/**
 * Get the IP of kubernetes from the environment variable [KUBERNETES_IP_ENV].
 * @return IP of kubernetes.
 */
fun getKubernetesIp(): String {
    return syspropOrEnv(KUBERNETES_IP_ENV) ?: throw IllegalStateException("$KUBERNETES_IP_ENV environment variable must be set")
}

/**
 * Get the token of kubernetes from the environment variable [KUBERNETES_TOKEN_ENV].
 * @return Token of kubernetes.
 */
fun getKubernetesToken(): String {
    return syspropOrEnv(KUBERNETES_TOKEN_ENV) ?: throw IllegalStateException("$KUBERNETES_TOKEN_ENV environment variable must be set")
}

/**
 * Get the CA certificate of kubernetes.
 * Walks from the project directory to the user's home directory
 * to find the kubernetes folder and the CA certificate file.
 * @return CA certificate of kubernetes in string format.
 */
fun getKubernetesCaCert(): String {
    return getKubernetesCaCertPath().readLines()
}

/**
 * Get the CA certificate path of kubernetes.
 * Walks from the project directory to the user's home directory
 * to find the kubernetes folder and the CA certificate file.
 * If the file is not found, an exception will be thrown.
 * @return Path of CA certificate of kubernetes.
 */
fun getKubernetesCaCertPath(): Path {
    val kubernetesFolderPath = findFolderInParent(MINIKUBE_FOLDER)
    require(kubernetesFolderPath != null && kubernetesFolderPath.exists()) { "$MINIKUBE_FOLDER folder not found" }

    val caCertPath = kubernetesFolderPath.resolve(CA_CERT_FILE)
    require(caCertPath.exists()) { "$CA_CERT_FILE file not found in $MINIKUBE_FOLDER folder" }
    return caCertPath
}
