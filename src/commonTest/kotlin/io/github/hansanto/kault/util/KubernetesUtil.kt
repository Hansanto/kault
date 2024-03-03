package io.github.hansanto.kault.util

import io.kotest.mpp.env
import kotlinx.io.files.Path

private const val MINIKUBE_IP_ENV = "MINIKUBE_IP"
private const val MINIKUBE_FOLDER = ".minikube"
private const val CA_CERT_FILE = "ca.crt"

fun getKubernetesHost(): String {
    return "http://${getMinikubeIp()}:8443"
}

fun getMinikubeIp(): String {
    return env(MINIKUBE_IP_ENV) ?: throw IllegalStateException("$MINIKUBE_IP_ENV environment variable must be set")
}

fun getKubernetesCaCert(): String {
    return getKubernetesCaCertPath().readLines()
}

fun getKubernetesCaCertPath(): Path {
    val minikubeFolderPath = findFolderInParent(MINIKUBE_FOLDER)
    require(minikubeFolderPath != null && minikubeFolderPath.exists()) { "$MINIKUBE_FOLDER folder not found" }

    val caCertPath = minikubeFolderPath.resolve(CA_CERT_FILE)
    require(caCertPath.exists()) { "$CA_CERT_FILE file not found in $MINIKUBE_FOLDER folder" }
    return caCertPath
}
