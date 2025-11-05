package io.github.hansanto.kault.auth.jwt

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.jwt.payload.OIDCConfigurePayload
import io.github.hansanto.kault.auth.jwt.response.OIDCConfigureResponse
import io.github.hansanto.kault.extension.decodeBodyJsonDataFieldObject
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @see VaultAuthOIDC.configure(payload)
 */
public suspend inline fun VaultAuthOIDC.configure(
    payloadBuilder: BuilderDsl<OIDCConfigurePayload.Builder>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = OIDCConfigurePayload.Builder().apply(payloadBuilder).build()
    return configure(payload)
}

/**
 * Provides methods for managing Kubernetes authentication within Vault.
 */
public interface VaultAuthOIDC {

    /**
     * The JWT authentication backend validates JWTs (or OIDC) using the configured credentials. If using OIDC Discovery, the URL must be provided, along with (optionally) the CA cert to use for the connection. If performing JWT validation locally, a set of public keys must be provided.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/jwt#configure)
     * @return Returns true if the configuration was updated successfully.
     */
    public suspend fun configure(payload: OIDCConfigurePayload): Boolean

    /**
     * Returns the previously configured config.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/jwt#read-config)
     * @return Response.
     */
    public suspend fun readConfiguration(): OIDCConfigureResponse
}

/**
 * Implementation of [VaultAuthOIDC].
 */
public class VaultAuthOIDCImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultAuthOIDC {

    public companion object {

        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultAuthOIDCImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "oidc"
    }

    /**
     * Builder class to simplify the creation of [VaultAuthOIDCImpl].
     */
    public open class Builder : ServiceBuilder<VaultAuthOIDCImpl>() {

        public override var path: String = Default.PATH

        public override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultAuthOIDCImpl =
            VaultAuthOIDCImpl(
                client = client,
                path = completePath
            )
    }

    override suspend fun configure(payload: OIDCConfigurePayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "config")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun readConfiguration(): OIDCConfigureResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "config")
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

}
