package io.github.hansanto.kault.system.auth

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.extension.decodeBodyJsonDataFieldObject
import io.github.hansanto.kault.system.auth.payload.AuthTuneConfigurationParametersPayload
import io.github.hansanto.kault.system.auth.payload.EnableMethodPayload
import io.github.hansanto.kault.system.auth.response.AuthReadConfigurationResponse
import io.github.hansanto.kault.system.auth.response.AuthReadTuningInformationResponse
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
 * @see VaultSystemAuth.enable(path, payload)
 */
public suspend inline fun VaultSystemAuth.enable(
    path: String,
    payloadBuilder: BuilderDsl<EnableMethodPayload.Builder>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = EnableMethodPayload.Builder().apply(payloadBuilder).build()
    return enable(path, payload)
}

/**
 * @see VaultSystemAuth.tune(path, payload)
 */
public suspend inline fun VaultSystemAuth.tune(
    path: String,
    payloadBuilder: BuilderDsl<AuthTuneConfigurationParametersPayload>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = AuthTuneConfigurationParametersPayload().apply(payloadBuilder)
    return tune(path, payload)
}

/**
 * Provides methods for managing authentication methods within Vault.
 */
public interface VaultSystemAuth {

    /**
     * This endpoint lists all enabled auth methods.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/auth#list-auth-methods)
     * @return Response.
     */
    public suspend fun list(): Map<String, AuthReadConfigurationResponse>

    /**
     * This endpoint enables a new auth method. After enabling, the auth method can be accessed and configured via the auth path specified as part of the URL.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/auth#enable-auth-method)
     * @param path Specifies the path in which to enable the auth method. This is part of the request URL.
     * @param payload Parameters for enabling the auth method.
     * @return Returns true if the auth method was successfully enabled.
     */
    public suspend fun enable(path: String, payload: EnableMethodPayload): Boolean

    /**
     * This endpoint returns the configuration of the auth method at the given path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/auth#read-auth-method-configuration)
     * @param path Specifies the path to read.
     * @return Response.
     */
    public suspend fun readConfiguration(path: String): AuthReadConfigurationResponse

    /**
     * This endpoint disables the auth method at the given auth path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/auth#disable-auth-method)
     * @param path Specifies the path to disable. This is part of the request URL.
     * @return Returns true if the auth method was successfully disabled.
     */
    public suspend fun disable(path: String): Boolean

    /**
     * This endpoint reads the given auth path's configuration. This endpoint requires sudo capability on the final path, but the same functionality can be achieved without sudo via sys/mounts/auth/[auth-path]/tune.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/auth#read-auth-method-tuning)
     * @param path Specifies the path in which to tune.
     * @return Response.
     */
    public suspend fun readTuning(path: String): AuthReadTuningInformationResponse

    /**
     * Tune configuration parameters for a given auth path. This endpoint requires sudo capability on the final path, but the same functionality can be achieved without sudo via sys/mounts/auth/[auth-path]/tune.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/auth#tune-auth-method)
     * @param path Specifies the path in which to tune.
     * @return Response.
     */
    public suspend fun tune(
        path: String,
        payload: AuthTuneConfigurationParametersPayload = AuthTuneConfigurationParametersPayload()
    ): Boolean
}

/**
 * Implementation of [VaultSystemAuth].
 */
public class VaultSystemAuthImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultSystemAuth {

    public companion object {

        /**
         * Creates a new instance of [VaultSystemAuthImpl] using the provided HttpClient and optional parent path.
         * @param client HttpClient to interact with API.
         * @param parentPath The optional parent path used for building the final path used to interact with endpoints.
         * @param builder Builder to define the authentication service.
         * @return The instance of [VaultSystemAuthImpl] that was built.
         */
        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultSystemAuthImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "auth"
    }

    /**
     * Builder class to simplify the creation of [VaultSystemAuthImpl].
     */
    public open class Builder : ServiceBuilder<VaultSystemAuthImpl>() {

        public override var path: String = Default.PATH

        override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultSystemAuthImpl =
            VaultSystemAuthImpl(
                client = client,
                path = completePath
            )
    }

    override suspend fun list(): Map<String, AuthReadConfigurationResponse> {
        val response = client.get {
            url {
                appendPathSegments(this@VaultSystemAuthImpl.path)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun enable(path: String, payload: EnableMethodPayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(this@VaultSystemAuthImpl.path, path)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun readConfiguration(path: String): AuthReadConfigurationResponse {
        val response = client.get {
            url {
                appendPathSegments(this@VaultSystemAuthImpl.path, path)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun disable(path: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(this@VaultSystemAuthImpl.path, path)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun readTuning(path: String): AuthReadTuningInformationResponse {
        val response = client.get {
            url {
                appendPathSegments(this@VaultSystemAuthImpl.path, path, "tune")
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun tune(path: String, payload: AuthTuneConfigurationParametersPayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(this@VaultSystemAuthImpl.path, path, "tune")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }
}
