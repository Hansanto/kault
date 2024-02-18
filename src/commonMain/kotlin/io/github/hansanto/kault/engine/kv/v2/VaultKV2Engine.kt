package io.github.hansanto.kault.engine.kv.v2

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.common.SecretVersion
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2ConfigureRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2DeleteVersionsRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2SubKeysRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2WriteMetadataRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2WriteRequest
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadConfigurationResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadMetadataResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadSubkeysResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2WriteResponse
import io.github.hansanto.kault.extension.MergePatchJson
import io.github.hansanto.kault.extension.decodeBodyJsonFieldObject
import io.github.hansanto.kault.extension.list
import io.github.hansanto.kault.response.StandardListResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @see VaultKV2Engine.createOrUpdateSecret(path, payload)
 */
public suspend inline fun VaultKV2Engine.createOrUpdateSecret(
    path: String,
    payloadBuilder: BuilderDsl<KvV2WriteRequest.Builder>
): KvV2WriteResponse {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = KvV2WriteRequest.Builder().apply(payloadBuilder).build()
    return createOrUpdateSecret(path, payload)
}

/**
 * @see VaultKV2Engine.patchSecret(path, payload)
 */
public suspend inline fun VaultKV2Engine.patchSecret(
    path: String,
    payloadBuilder: BuilderDsl<KvV2WriteRequest.Builder>
): KvV2WriteResponse {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = KvV2WriteRequest.Builder().apply(payloadBuilder).build()
    return patchSecret(path, payload)
}

/**
 * @see VaultKV2Engine.readSecretSubKeys(path, payload)
 */
public suspend inline fun VaultKV2Engine.readSecretSubKeys(
    path: String,
    payloadBuilder: BuilderDsl<KvV2SubKeysRequest>
): KvV2ReadSubkeysResponse {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = KvV2SubKeysRequest().apply(payloadBuilder)
    return readSecretSubKeys(path, payload)
}

/**
 * @see VaultKV2Engine.createOrUpdateMetadata(path, payload)
 */
public suspend inline fun VaultKV2Engine.createOrUpdateMetadata(
    path: String,
    payloadBuilder: BuilderDsl<KvV2WriteMetadataRequest>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = KvV2WriteMetadataRequest().apply(payloadBuilder)
    return createOrUpdateMetadata(path, payload)
}

/**
 * @see VaultKV2Engine.patchMetadata(path, payload)
 */
public suspend inline fun VaultKV2Engine.patchMetadata(
    path: String,
    payloadBuilder: BuilderDsl<KvV2WriteMetadataRequest>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = KvV2WriteMetadataRequest().apply(payloadBuilder)
    return patchMetadata(path, payload)
}

/**
 * Provides methods for managing the KV version 2 secrets engine.
 * [Documentation](https://www.vaultproject.io/api-docs/secret/kv/kv-v2)
 */
public interface VaultKV2Engine {

    /**
     * This path configures backend level settings that are applied to every key in the key-value store.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#configure-the-kv-engine)
     * @param payload Payload to configure the KV engine.
     * @return True if the configuration was updated.
     */
    public suspend fun configure(payload: KvV2ConfigureRequest = KvV2ConfigureRequest()): Boolean

    /**
     * This path retrieves the current configuration for the secrets backend at the given path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-kv-engine-configuration)
     * @return Response.
     */
    public suspend fun readConfiguration(): KvV2ReadConfigurationResponse

    /**
     * This endpoint retrieves the secret at the specified location. The metadata fields created_time, deletion_time, destroyed, and version are version specific. The custom_metadata field is part of the secret's key metadata and is included in the response whether or not the calling token has read access to the associated metadata endpoint.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-secret-version)
     * @param path Specifies the path of the secret to read. This is specified as part of the URL.
     * @param version Specifies the version to return. If not set the latest version is returned.
     * @return Response.
     */
    public suspend fun readSecret(path: String, version: SecretVersion? = null): KvV2ReadResponse

    /**
     * This endpoint creates a new version of a secret at the specified location. If the value does not yet exist, the calling token must have an ACL policy granting the create capability. If the value already exists, the calling token must have an ACL policy granting the update capability.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#create-update-secret)
     * @param path Specifies the path of the secret to update. This is specified as part of the URL.
     * @param payload New secret to create or update.
     * @return Response.
     */
    public suspend fun createOrUpdateSecret(path: String, payload: KvV2WriteRequest): KvV2WriteResponse

    /**
     * This endpoint provides the ability to patch an existing secret at the specified location. The secret must neither be deleted nor destroyed. The calling token must have an ACL policy granting the patch capability. Currently, only JSON merge patch is supported and must be specified using a Content-Type header value of application/merge-patch+json. A new version will be created upon successfully applying a patch with the provided data.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#patch-secret)
     * @param path Specifies the path of the secret to patch. This is specified as part of the URL.
     * @param payload Part of the secret to update.
     * @return Response.
     */
    public suspend fun patchSecret(path: String, payload: KvV2WriteRequest): KvV2WriteResponse

    /**
     * This endpoint provides the subkeys within a secret entry that exists at the requested path. The secret entry at this path will be retrieved and stripped of all data by replacing underlying values of leaf keys (i.e. non-map keys or map keys with no underlying subkeys) with null.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-secret-subkeys)
     * @param path Specifies the path of the secret to read. This is specified as part of the URL.
     * @param payload Specifies the options for reading the secret subkeys.
     * @return Response.
     */
    public suspend fun readSecretSubKeys(path: String, payload: KvV2SubKeysRequest = KvV2SubKeysRequest()): KvV2ReadSubkeysResponse

    /**
     * This endpoint issues a soft delete of the secret's latest version at the specified location. This marks the version as deleted and will stop it from being returned from reads, but the underlying data will not be removed. A delete can be undone using the undelete path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#delete-latest-version-of-secret)
     * @param path Specifies the path of the secret to delete. This is specified as part of the URL.
     * @return True if the latest version was deleted.
     */
    public suspend fun deleteSecretLatestVersion(path: String): Boolean

    /**
     * This endpoint issues a soft delete of the specified versions of the secret. This marks the versions as deleted and will stop them from being returned from reads, but the underlying data will not be removed. A delete can be undone using the undelete path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#delete-secret-versions)
     * @param path Specifies the path of the secret to delete. This is specified as part of the URL.
     * @param versions The versions to be deleted. The versioned data will not be deleted, but it will no longer be returned in normal get requests.
     * @return True if the versions were deleted.
     */
    public suspend fun deleteSecretVersions(path: String, versions: List<SecretVersion>): Boolean

    /**
     * Undeletes the data for the provided version and path in the key-value store. This restores the data, allowing it to be returned on get requests.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#undelete-secret-versions)
     * @param path Specifies the path of the secret to undelete. This is specified as part of the URL.
     * @param versions The versions to undelete. The versions will be restored and their data will be returned on normal get requests.
     * @return True if the versions were undeleted.
     */
    public suspend fun undeleteSecretVersions(path: String, versions: List<SecretVersion>): Boolean

    /**
     * Permanently removes the specified version data for the provided key and version numbers from the key-value store.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#destroy-secret-versions)
     * @param path Specifies the path of the secret to destroy. This is specified as part of the URL.
     * @param versions The versions to destroy. Their data will be permanently deleted.
     * @return True if the versions were destroyed.
     */
    public suspend fun destroySecretVersions(path: String, versions: List<SecretVersion>): Boolean

    /**
     * This endpoint returns a list of key names at the specified location. Folders are suffixed with /. The input must be a folder; list on a file will not return a value. Note that no policy-based filtering is performed on keys; do not encode sensitive information in key names. The values themselves are not accessible via this command.
     * To list secrets for KV v2, a user must have a policy granting them the list capability on this /metadata/ path - even if all the rest of their interactions with the KV v2 are via the /data/ APIs. Access to at least list the /metadata/ path should typically also be granted.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#list-secrets)
     * @param path Specifies the path of the secrets to list. This is specified as part of the URL.
     * @return List of keys.
     */
    public suspend fun listSecrets(path: String): List<String>

    /**
     * This endpoint retrieves the metadata and versions for the secret at the specified path. Metadata is version-agnostic.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-secret-metadata)
     * @param path Specifies the path of the secret to read. This is specified as part of the URL.
     * @return Response.
     */
    public suspend fun readSecretMetadata(path: String): KvV2ReadMetadataResponse

    /**
     * This endpoint creates or updates the metadata of a secret at the specified location. It does not create a new version.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#create-update-metadata)
     * @param path Specifies the path of the secret to update. This is specified as part of the URL.
     * @param payload Payload to update the metadata.
     * @return True if the metadata was updated.
     */
    public suspend fun createOrUpdateMetadata(path: String, payload: KvV2WriteMetadataRequest = KvV2WriteMetadataRequest()): Boolean

    /**
     * This endpoint patches an existing metadata entry of a secret at the specified location. The calling token must have an ACL policy granting the patch capability. Currently, only JSON merge patch is supported and must be specified using a Content-Type header value of application/merge-patch+json. It does not create a new version.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#patch-metadata)
     * @param path Specifies the path of the secret to patch. This is specified as part of the URL.
     * @param payload Payload to update the metadata.
     * @return True if the metadata was updated.
     */
    public suspend fun patchMetadata(path: String, payload: KvV2WriteMetadataRequest = KvV2WriteMetadataRequest()): Boolean

    /**
     * This endpoint permanently deletes the key metadata and all version data for the specified key. All version history will be removed.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#delete-metadata-and-all-versions)
     * @param path Specifies the path of the secret to delete. This is specified as part of the URL.
     * @return True if the metadata and all versions were deleted.
     */
    public suspend fun deleteMetadataAndAllVersions(path: String): Boolean
}

/**
 * Implementation of [VaultKV2Engine].
 */
public class VaultKV2EngineImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultKV2Engine {

    public companion object {

        /**
         * Creates a new instance of [VaultKV2EngineImpl] using the provided HttpClient and optional parent path.
         * @param client HttpClient to interact with API.
         * @param parentPath The optional parent path used for building the final path used to interact with endpoints.
         * @param builder Builder to define the authentication service.
         * @return The instance of [VaultKV2EngineImpl] that was built.
         */
        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultKV2EngineImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "secret"
    }

    /**
     * Builder class to simplify the creation of [VaultKV2EngineImpl].
     */
    public class Builder : ServiceBuilder<VaultKV2EngineImpl>() {

        public override var path: String = Default.PATH

        override fun buildWithFullPath(client: HttpClient, fullPath: String): VaultKV2EngineImpl = VaultKV2EngineImpl(
            client = client,
            path = fullPath
        )
    }

    override suspend fun configure(payload: KvV2ConfigureRequest): Boolean {
        val response = client.post {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "config")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun readConfiguration(): KvV2ReadConfigurationResponse {
        val response = client.get {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "config")
            }
        }
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun readSecret(path: String, version: Long?): KvV2ReadResponse {
        val response = client.get {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "data", path)
                parameter("version", version)
            }
        }
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun createOrUpdateSecret(path: String, payload: KvV2WriteRequest): KvV2WriteResponse {
        val response = client.post {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "data", path)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun patchSecret(path: String, payload: KvV2WriteRequest): KvV2WriteResponse {
        val response = client.patch {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "data", path)
            }
            contentType(ContentType.Application.MergePatchJson)
            setBody(payload)
        }
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun readSecretSubKeys(path: String, payload: KvV2SubKeysRequest): KvV2ReadSubkeysResponse {
        val response = client.get {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "subkeys", path)
            }
            parameter("version", payload.version)
            parameter("depth", payload.depth)
        }
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun deleteSecretLatestVersion(path: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "data", path)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun deleteSecretVersions(path: String, versions: List<SecretVersion>): Boolean {
        val response = client.post {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "delete", path)
            }
            contentType(ContentType.Application.Json)
            setBody(KvV2DeleteVersionsRequest(versions))
        }
        return response.status.isSuccess()
    }

    override suspend fun undeleteSecretVersions(path: String, versions: List<SecretVersion>): Boolean {
        val response = client.post {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "undelete", path)
            }
            contentType(ContentType.Application.Json)
            setBody(KvV2DeleteVersionsRequest(versions))
        }
        return response.status.isSuccess()
    }

    override suspend fun destroySecretVersions(path: String, versions: List<SecretVersion>): Boolean {
        val response = client.put {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "destroy", path)
            }
            contentType(ContentType.Application.Json)
            setBody(KvV2DeleteVersionsRequest(versions))
        }
        return response.status.isSuccess()
    }

    override suspend fun listSecrets(path: String): List<String> {
        val response = client.list {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "metadata", path)
            }
        }
        return response.decodeBodyJsonFieldObject<StandardListResponse>("data", VaultClient.json).keys
    }

    override suspend fun readSecretMetadata(path: String): KvV2ReadMetadataResponse {
        val response = client.get {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "metadata", path)
            }
        }
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun createOrUpdateMetadata(path: String, payload: KvV2WriteMetadataRequest): Boolean {
        val response = client.post {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "metadata", path)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun patchMetadata(path: String, payload: KvV2WriteMetadataRequest): Boolean {
        val response = client.patch {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "metadata", path)
            }
            contentType(ContentType.Application.MergePatchJson)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun deleteMetadataAndAllVersions(path: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(this@VaultKV2EngineImpl.path, "metadata", path)
            }
        }
        return response.status.isSuccess()
    }
}
