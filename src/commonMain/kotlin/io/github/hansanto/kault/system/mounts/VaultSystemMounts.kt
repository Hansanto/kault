package io.github.hansanto.kault.system.mounts

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.extension.decodeBodyJsonDataFieldObject
import io.github.hansanto.kault.system.mounts.payload.MountsEnableSecretsEnginePayload
import io.github.hansanto.kault.system.mounts.payload.MountsTuneConfigurationPayload
import io.github.hansanto.kault.system.mounts.response.MountsGetConfigurationOfSecretEngineResponse
import io.github.hansanto.kault.system.mounts.response.MountsListMountedSecretsEnginesResponse
import io.github.hansanto.kault.system.mounts.response.MountsReadConfigurationResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @see VaultSystemMounts.enableSecretsEngine(payload)
 */
public suspend inline fun VaultSystemMounts.enableSecretsEngine(
    path: String,
    payloadBuilder: BuilderDsl<MountsEnableSecretsEnginePayload.Builder>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = MountsEnableSecretsEnginePayload.Builder().apply(payloadBuilder).build()
    return enableSecretsEngine(path, payload)
}

/**
 * @see VaultSystemMounts.tuneMountConfiguration(path, payload)
 */
public suspend inline fun VaultSystemMounts.tuneMountConfiguration(
    path: String,
    payloadBuilder: BuilderDsl<MountsTuneConfigurationPayload>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = MountsTuneConfigurationPayload().apply(payloadBuilder)
    return tuneMountConfiguration(path, payload)
}

public interface VaultSystemMounts {

    /**
     * This endpoint lists all the mounted secrets engines.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/mounts#list-mounted-secrets-engines)
     * @return Response.
     */
    public suspend fun listMountedSecretsEngines(): MountsListMountedSecretsEnginesResponse

    /**
     * This endpoint enables a new secrets engine at the given path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/mounts#enable-secrets-engine)
     * @param path Path to enable the secrets engine at. This is a URL parameter, not part of the payload.
     * @param payload Payload to enable the secrets engine.
     * @return `true` if the request was successful, `false` otherwise.
     */
    public suspend fun enableSecretsEngine(path: String, payload: MountsEnableSecretsEnginePayload): Boolean

    /**
     * This endpoint disables the mount point specified in the URL.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/mounts#disable-secrets-engine)
     * @param path Path of the secrets engine to disable.
     * @return `true` if the request was successful, `false` otherwise.
     */
    public suspend fun disableSecretsEngine(path: String): Boolean

    /**
     * This endpoint returns the configuration of a specific secret engine.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/mounts#get-the-configuration-of-a-secret-engine)
     * @param path Path of the secrets engine to get the configuration of.
     * @return Response.
     */
    public suspend fun getConfigurationOfSecretEngine(path: String): MountsGetConfigurationOfSecretEngineResponse

    /**
     * This endpoint reads the given mount's configuration.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/mounts#read-mount-configuration)
     * @param path Path of the mount to read the configuration of.
     * @return Response.
     */
    public suspend fun readMountConfiguration(path: String): MountsReadConfigurationResponse

    /**
     * This endpoint tunes configuration parameters for a given mount point.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/mounts#tune-mount-configuration)
     * @param path Path of the mount to tune the configuration of.
     * @param payload Payload to tune the configuration of the mount.
     * @return `true` if the request was successful, `false` otherwise.
     */
    public suspend fun tuneMountConfiguration(path: String, payload: MountsTuneConfigurationPayload): Boolean
}

/**
 * Implementation of [VaultSystemMounts].
 */
public class VaultSystemMountsImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultSystemMounts {
    public companion object {

        /**
         * Creates a new instance of [VaultSystemMountsImpl] using the provided HttpClient and optional parent path.
         * @param client HttpClient to interact with API.
         * @param parentPath The optional parent path used for building the final path used to interact with endpoints.
         * @param builder Builder to define the authentication service.
         * @return The instance of [VaultSystemMountsImpl] that was built.
         */
        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultSystemMountsImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "mounts"
    }

    /**
     * Builder class to simplify the creation of [VaultSystemMountsImpl].
     */
    public open class Builder : ServiceBuilder<VaultSystemMountsImpl>() {

        public override var path: String = Default.PATH

        override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultSystemMountsImpl =
            VaultSystemMountsImpl(
                client = client,
                path = completePath
            )
    }

    override suspend fun listMountedSecretsEngines(): MountsListMountedSecretsEnginesResponse {
        val response = client.get {
            url {
                appendPathSegments(this@VaultSystemMountsImpl.path)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun enableSecretsEngine(path: String, payload: MountsEnableSecretsEnginePayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(this@VaultSystemMountsImpl.path, path)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun disableSecretsEngine(path: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(this@VaultSystemMountsImpl.path, path)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun getConfigurationOfSecretEngine(path: String): MountsGetConfigurationOfSecretEngineResponse {
        val response = client.get {
            url {
                appendPathSegments(this@VaultSystemMountsImpl.path, path)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun readMountConfiguration(path: String): MountsReadConfigurationResponse {
        val response = client.get {
            url {
                appendPathSegments(this@VaultSystemMountsImpl.path, path, "tune")
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun tuneMountConfiguration(path: String, payload: MountsTuneConfigurationPayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(this@VaultSystemMountsImpl.path, path, "tune")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }
}
