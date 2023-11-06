package com.github.hansanto.kault.system.auth

import com.github.hansanto.kault.ServiceBuilder
import com.github.hansanto.kault.ServiceBuilderConstructor
import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.extension.decodeBodyJsonFieldObject
import com.github.hansanto.kault.system.auth.payload.EnableMethodPayload
import com.github.hansanto.kault.system.auth.response.AuthReadConfigurationResponse
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
    payloadBuilder: EnableMethodPayload.Builder.() -> Unit
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = EnableMethodPayload.Builder().apply(payloadBuilder).build()
    return enable(path, payload)
}

public interface VaultSystemAuth {

    /**
     * This endpoint lists all enabled auth methods.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/auth#list-auth-methods)
     * @return Response.
     */
    public suspend fun list(): Any

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
    public suspend fun readTuning(path: String): Any

    /**
     * Tune configuration parameters for a given auth path. This endpoint requires sudo capability on the final path, but the same functionality can be achieved without sudo via sys/mounts/auth/[auth-path]/tune.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/auth#tune-auth-method)
     * @param path Specifies the path in which to tune.
     * @return Response.
     */
    public suspend fun tune(path: String, payload: Any): Any
}

public class VaultSystemAuthImpl(
    private val client: HttpClient,
    public val path: String
) : VaultSystemAuth {

    public companion object : ServiceBuilderConstructor<VaultSystemAuthImpl, Builder> {

        public override operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: Builder.() -> Unit
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
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder : ServiceBuilder<VaultSystemAuthImpl>() {

        public override var path: String = Default.PATH

        override fun buildWithFullPath(client: HttpClient, fullPath: String): VaultSystemAuthImpl = VaultSystemAuthImpl(
            client = client,
            path = fullPath
        )
    }

    override suspend fun list(): Any {
        TODO("Not yet implemented")
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
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun disable(path: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(this@VaultSystemAuthImpl.path, path)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun readTuning(path: String): Any {
        TODO("Not yet implemented")
    }

    override suspend fun tune(path: String, payload: Any): Any {
        TODO("Not yet implemented")
    }
}
