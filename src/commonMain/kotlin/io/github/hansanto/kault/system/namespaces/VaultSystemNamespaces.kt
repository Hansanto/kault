package io.github.hansanto.kault.system.namespaces

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.extension.decodeBodyJsonDataFieldObject
import io.github.hansanto.kault.extension.list
import io.github.hansanto.kault.response.StandardListResponse
import io.github.hansanto.kault.system.namespaces.payload.CreateNamespacePayload
import io.github.hansanto.kault.system.namespaces.payload.PatchNamespacePayload
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

public interface VaultSystemNamespaces {

    /**
     * This endpoints lists all the namespaces.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/namespaces#list-namespaces)
     * @return Response.
     */
    public suspend fun list(): StandardListResponse

    /**
     * This endpoint creates a namespace at the given path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/namespaces#create-namespace)
     * @param path Specifies the path where the namespace will be created.
     * @param customMetadata A map of arbitrary string to string valued user-provided metadata meant to describe the namespace.
     * @return Response.
     */
    public suspend fun create(path: String, customMetadata: Map<String, String>? = null): Boolean

    /**
     * This endpoint patches an existing namespace at the specified path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/namespaces#patch-namespace)
     * @param path Specifies the path where the namespace will be created.
     * @param customMetadata A map of arbitrary string to string valued user-provided metadata meant to describe the namespace.
     * @return Response.
     */
    public suspend fun patch(path: String, customMetadata: Map<String, String>? = null): Boolean

    /**
     * This endpoint deletes a namespace at the specified path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/namespaces#delete-namespace)
     * @param path Specifies the path where the namespace will be deleted.
     * @return Response.
     */
    public suspend fun delete(path: String): Boolean

    /**
     * This endpoint gets the metadata for the given namespace path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/namespaces#read-namespace-information)
     * @param path Specifies the path where the namespace is located.
     * @return Response.
     */
    public suspend fun readInformation(path: String): Any

    /**
     * This endpoint locks the API for the current namespace path or optional subpath. The behavior when interacting with Vault from a locked namespace is described in [API Locked Response](https://developer.hashicorp.com/vault/docs/concepts/namespace-api-lock#api-locked-response).
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/namespaces#lock-namespace)
     * @param path Specifies the path where the namespace is located.
     * @return Response.
     */
    public suspend fun lock(path: String): Any

    /**
     * This endpoint unlocks the api for the current namespace path or optional subpath.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/namespaces#unlock-namespace)
     * @param path Specifies the path where the namespace is located.
     * @return Response.
     */
    public suspend fun unlock(path: String): Any

}

/**
 * Implementation of [VaultSystemNamespaces].
 */
public class VaultSystemNamespacesImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultSystemNamespaces {

    public companion object {

        /**
         * Creates a new instance of [VaultSystemNamespacesImpl] using the provided HttpClient and optional parent path.
         * @param client HttpClient to interact with API.
         * @param parentPath The optional parent path used for building the final path used to interact with endpoints.
         * @param builder Builder to define the authentication service.
         * @return The instance of [VaultSystemNamespacesImpl] that was built.
         */
        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultSystemNamespacesImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "namespaces"
    }

    /**
     * Builder class to simplify the creation of [VaultSystemNamespacesImpl].
     */
    public open class Builder : ServiceBuilder<VaultSystemNamespacesImpl>() {

        public override var path: String = Default.PATH

        override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultSystemNamespacesImpl =
            VaultSystemNamespacesImpl(
                client = client,
                path = completePath
            )
    }

    override suspend fun list(): StandardListResponse {
        val response = client.list {
            url {
                appendPathSegments(this@VaultSystemNamespacesImpl.path)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun create(path: String, customMetadata: Map<String, String>?): Boolean {
        val payload = CreateNamespacePayload(customMetadata = customMetadata)
        val response = client.post {
            url {
                appendPathSegments(this@VaultSystemNamespacesImpl.path, path)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun patch(path: String, customMetadata: Map<String, String>?): Boolean {
        val payload = PatchNamespacePayload(customMetadata = customMetadata)
        val response = client.patch {
            url {
                appendPathSegments(this@VaultSystemNamespacesImpl.path, path)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun delete(path: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(this@VaultSystemNamespacesImpl.path, path)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun readInformation(path: String): Any {
        val response = client.get {
            url {
                appendPathSegments(this@VaultSystemNamespacesImpl.path, path)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun lock(path: String): Any {
        val response = client.post {
            url {
                appendPathSegments(this@VaultSystemNamespacesImpl.path, "api-lock", "lock", path)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun unlock(path: String): Any {
        val response = client.post {
            url {
                appendPathSegments(this@VaultSystemNamespacesImpl.path, "api-lock", "unlock", path)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }
}
