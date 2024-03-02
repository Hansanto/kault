package io.github.hansanto.kault.auth.kubernetes

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.kubernetes.payload.KubernetesConfigureAuthPayload
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess

/**
 * Provides methods for managing Kubernetes authentication within Vault.
 */
public interface VaultAuthKubernetes {

    /**
     * This endpoint configures the public key used to validate the JWT signature and the necessary information to access the Kubernetes API.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/kubernetes#configure-method)
     * @return Returns true if the configuration was updated successfully.
     */
    public suspend fun configure(payload: KubernetesConfigureAuthPayload): Boolean
}

/**
 * Implementation of [VaultAuthKubernetes].
 */
public class VaultAuthKubernetesImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultAuthKubernetes {

    public companion object {

        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultAuthKubernetesImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "kubernetes"
    }

    /**
     * Builder class to simplify the creation of [VaultAuthKubernetesImpl].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder : ServiceBuilder<VaultAuthKubernetesImpl>() {

        public override var path: String = Default.PATH

        public override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultAuthKubernetesImpl =
            VaultAuthKubernetesImpl(
                client = client,
                path = completePath
            )
    }

    override suspend fun configure(payload: KubernetesConfigureAuthPayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "config")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }
}
