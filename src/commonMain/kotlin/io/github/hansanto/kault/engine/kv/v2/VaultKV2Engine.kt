package io.github.hansanto.kault.engine.kv.v2

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.ktor.client.HttpClient

/**
 * Provides methods for managing the KV version 2 secrets engine.
 * [Documentation](https://www.vaultproject.io/api-docs/secret/kv/kv-v2)
 */
public interface VaultKV2Engine {

    /**
     * This path configures backend level settings that are applied to every key in the key-value store.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#configure-the-kv-engine)
     * @param payload TODO
     * @return TODO
     */
    public suspend fun configure(payload: Any): Any

    /**
     * This path retrieves the current configuration for the secrets backend at the given path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-kv-engine-configuration)
     * @return TODO
     */
    public suspend fun readConfiguration(): Any

    /**
     * This endpoint retrieves the secret at the specified location. The metadata fields created_time, deletion_time, destroyed, and version are version specific. The custom_metadata field is part of the secret's key metadata and is included in the response whether or not the calling token has read access to the associated metadata endpoint.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-secret-version)
     * @param path Specifies the path of the secret to read. This is specified as part of the URL.
     * @return TODO
     */
    public suspend fun readSecret(path: String): Any

    /**
     * This endpoint creates a new version of a secret at the specified location. If the value does not yet exist, the calling token must have an ACL policy granting the create capability. If the value already exists, the calling token must have an ACL policy granting the update capability.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#create-update-secret)
     * @param path Specifies the path of the secret to update. This is specified as part of the URL.
     * @param payload TODO
     * @return TODO
     */
    public suspend fun createOrUpdateSecret(path: String, payload: Any): Any

    /**
     * This endpoint provides the ability to patch an existing secret at the specified location. The secret must neither be deleted nor destroyed. The calling token must have an ACL policy granting the patch capability. Currently, only JSON merge patch is supported and must be specified using a Content-Type header value of application/merge-patch+json. A new version will be created upon successfully applying a patch with the provided data.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#patch-secret)
     * @param path Specifies the path of the secret to patch. This is specified as part of the URL.
     * @param payload TODO
     * @return TODO
     */
    public suspend fun patchSecret(path: String, payload: Any): Any

    /**
     * This endpoint provides the subkeys within a secret entry that exists at the requested path. The secret entry at this path will be retrieved and stripped of all data by replacing underlying values of leaf keys (i.e. non-map keys or map keys with no underlying subkeys) with null.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-secret-subkeys)
     * @param path Specifies the path of the secret to read. This is specified as part of the URL.
     * @return TODO
     */
    public suspend fun readSecretSubKeys(path: String): Any

    /**
     * This endpoint issues a soft delete of the secret's latest version at the specified location. This marks the version as deleted and will stop it from being returned from reads, but the underlying data will not be removed. A delete can be undone using the undelete path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#delete-latest-version-of-secret)
     * @param path Specifies the path of the secret to delete. This is specified as part of the URL.
     * @return TODO
     */
    public suspend fun deleteSecretLatestVersion(path: String): Any

    /**
     * This endpoint issues a soft delete of the specified versions of the secret. This marks the versions as deleted and will stop them from being returned from reads, but the underlying data will not be removed. A delete can be undone using the undelete path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#delete-secret-versions)
     * @param path Specifies the path of the secret to delete. This is specified as part of the URL.
     * @param versions The versions to be deleted. The versioned data will not be deleted, but it will no longer be returned in normal get requests.
     * @return TODO
     */
    public suspend fun deleteSecretVersions(path: String, versions: List<Int>): Any

    /**
     * Undeletes the data for the provided version and path in the key-value store. This restores the data, allowing it to be returned on get requests.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#undelete-secret-versions)
     * @param path Specifies the path of the secret to undelete. This is specified as part of the URL.
     * @param versions The versions to undelete. The versions will be restored and their data will be returned on normal get requests.
     * @return TODO
     */
    public suspend fun undeleteSecretVersions(path: String, versions: List<Int>): Any

    /**
     * Permanently removes the specified version data for the provided key and version numbers from the key-value store.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#destroy-secret-versions)
     * @param path Specifies the path of the secret to destroy. This is specified as part of the URL.
     * @param versions The versions to destroy. Their data will be permanently deleted.
     * @return TODO
     */
    public suspend fun destroySecretVersions(path: String, versions: List<Int>): Any

    /**
     * This endpoint returns a list of key names at the specified location. Folders are suffixed with /. The input must be a folder; list on a file will not return a value. Note that no policy-based filtering is performed on keys; do not encode sensitive information in key names. The values themselves are not accessible via this command.
     * To list secrets for KV v2, a user must have a policy granting them the list capability on this /metadata/ path - even if all the rest of their interactions with the KV v2 are via the /data/ APIs. Access to at least list the /metadata/ path should typically also be granted.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#list-secrets)
     * @param path Specifies the path of the secrets to list. This is specified as part of the URL.
     * @return TODO
     */
    public suspend fun listSecrets(path: String): Any

    /**
     * This endpoint retrieves the metadata and versions for the secret at the specified path. Metadata is version-agnostic.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#read-secret-metadata)
     * @param path Specifies the path of the secret to read. This is specified as part of the URL.
     * @return TODO
     */
    public suspend fun readSecretMetadata(path: String): Any

    /**
     * This endpoint creates or updates the metadata of a secret at the specified location. It does not create a new version.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#create-update-metadata)
     * @param path Specifies the path of the secret to update. This is specified as part of the URL.
     * @param payload TODO
     * @return TODO
     */
    public suspend fun createOrUpdateMetadata(path: String, payload: Any): Any

    /**
     * This endpoint patches an existing metadata entry of a secret at the specified location. The calling token must have an ACL policy granting the patch capability. Currently, only JSON merge patch is supported and must be specified using a Content-Type header value of application/merge-patch+json. It does not create a new version.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2#patch-metadata)
     * @param path Specifies the path of the secret to patch. This is specified as part of the URL.
     * @param payload TODO
     * @return TODO
     */
    public suspend fun patchMetadata(path: String, payload: Any): Any

    /**
     * This endpoint permanently deletes the key metadata and all version data for the specified key. All version history will be removed.
     * @param path Specifies the path of the secret to delete. This is specified as part of the URL.
     * @return TODO
     */
    public suspend fun deleteMetadataAndAllVersions(path: String): Any
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

        /**
         * The path to the KV mount to interact with, such as secret. This is specified as part of the URL.
         * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2)
         */
        public override var path: String = Default.PATH

        override fun buildWithFullPath(client: HttpClient, fullPath: String): VaultKV2EngineImpl = VaultKV2EngineImpl(
            client = client,
            path = fullPath
        )
    }
}
