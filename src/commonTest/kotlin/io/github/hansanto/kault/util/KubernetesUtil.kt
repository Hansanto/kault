package io.github.hansanto.kault.util

import io.kotest.mpp.syspropOrEnv
import kotlinx.io.files.Path

/**
 * Environment variable name to retrieve IP of minikube.
 */
private const val MINIKUBE_IP_ENV = "MINIKUBE_IP"

/**
 * Environment variable name to retrieve port of minikube.
 */
private const val MINIKUBE_PORT_ENV = "MINIKUBE_PORT"

/**
 * Default port of minikube, check [documentation](https://minikube.sigs.k8s.io/docs/commands/start/).
 */
private const val MINIKUBE_DEFAULT_PORT = 8443

/**
 * Folder name of minikube in user's home directory.
 */
private const val MINIKUBE_FOLDER = ".minikube"

/**
 * File name of CA certificate in minikube folder.
 */
private const val CA_CERT_FILE = "ca.crt"

/**
 * Get the kubernetes host URL.
 * @param port Port of minikube, default is [MINIKUBE_DEFAULT_PORT].
 * @return URL of kubernetes host.
 */
fun getKubernetesHost(port: Int = syspropOrEnv(MINIKUBE_PORT_ENV)?.toInt() ?: MINIKUBE_DEFAULT_PORT): String {
    return "http://${getKubernetesIp()}:$port"
}

/**
 * Get the IP of minikube from the environment variable [MINIKUBE_IP_ENV].
 * @return IP of minikube.
 */
fun getKubernetesIp(): String {
    return syspropOrEnv(MINIKUBE_IP_ENV) ?: throw IllegalStateException("$MINIKUBE_IP_ENV environment variable must be set")
}

/**
 * Get the CA certificate of minikube.
 * Walks from the project directory to the user's home directory
 * to find the minikube folder and the CA certificate file.
 * @return CA certificate of minikube in string format.
 */
fun getKubernetesCaCert(): String {
    return getKubernetesCaCertPath().readLines()
}

/**
 * Get the CA certificate path of minikube.
 * Walks from the project directory to the user's home directory
 * to find the minikube folder and the CA certificate file.
 * If the file is not found, an exception will be thrown.
 * @return Path of CA certificate of minikube.
 */
fun getKubernetesCaCertPath(): Path {
    val minikubeFolderPath = findFolderInParent(MINIKUBE_FOLDER)
    require(minikubeFolderPath != null && minikubeFolderPath.exists()) { "$MINIKUBE_FOLDER folder not found" }

    val caCertPath = minikubeFolderPath.resolve(CA_CERT_FILE)
    require(caCertPath.exists()) { "$CA_CERT_FILE file not found in $MINIKUBE_FOLDER folder" }
    return caCertPath
}
