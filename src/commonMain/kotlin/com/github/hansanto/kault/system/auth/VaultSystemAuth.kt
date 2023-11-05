package com.github.hansanto.kault.system.auth

import com.github.hansanto.kault.ServiceBuilder
import com.github.hansanto.kault.ServiceBuilderConstructor
import com.github.hansanto.kault.system.auth.payload.EnableMethodPayload
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @see VaultSystemAuth.enableMethod(path, payload)
 */
public suspend inline fun VaultSystemAuth.enableMethod(
    path: String,
    payloadBuilder: EnableMethodPayload.() -> Unit
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = EnableMethodPayload().apply(payloadBuilder)
    return enableMethod(path, payload)
}

public interface VaultSystemAuth {

    /**
     * This endpoint enables a new auth method. After enabling, the auth method can be accessed and configured via the auth path specified as part of the URL.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/auth#enable-auth-method)
     * @param path Specifies the path in which to enable the auth method. This is part of the request URL.
     * @param payload Parameters for enabling the auth method.
     * @return Returns true if the auth method was successfully enabled.
     */
    public suspend fun enableMethod(path: String, payload: EnableMethodPayload): Boolean
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

    override suspend fun enableMethod(path: String, payload: EnableMethodPayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(this@VaultSystemAuthImpl.path, path)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }
}
