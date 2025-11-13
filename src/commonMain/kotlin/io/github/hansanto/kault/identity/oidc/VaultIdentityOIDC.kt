package io.github.hansanto.kault.identity.oidc

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.identity.oidc.payload.OIDCCreateOrUpdateProviderPayload
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
 * @see VaultIdentityOIDC.createOrUpdateProvider(name, payload)
 */
public suspend inline fun VaultIdentityOIDC.createOrUpdateProvider(
    name: String,
    builder: BuilderDsl<OIDCCreateOrUpdateProviderPayload>
): Boolean {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    val payload = OIDCCreateOrUpdateProviderPayload().apply(builder)
    return createOrUpdateProvider(name, payload)
}

public interface VaultIdentityOIDC {

    /**
     * This endpoint creates or updates a Provider.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#create-or-update-a-provider)
     * @param name The name of the provider.
     * @param payload The configuration payload for the OIDC provider.
     * @return Returns true if the configuration was updated successfully.
     */
    public suspend fun createOrUpdateProvider(name: String, payload: OIDCCreateOrUpdateProviderPayload): Boolean
}

/**
 * Implementation of [VaultIdentityOIDC].
 */
public class VaultIdentityOIDCImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultIdentityOIDC {

    public companion object {

        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultIdentityOIDCImpl = Builder().apply(builder).build(client, parentPath)
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
     * Builder class to simplify the creation of [VaultIdentityOIDCImpl].
     */
    public open class Builder : ServiceBuilder<VaultIdentityOIDCImpl>() {

        public override var path: String = Default.PATH

        public override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultIdentityOIDCImpl =
            VaultIdentityOIDCImpl(
                client = client,
                path = completePath
            )
    }

    override suspend fun createOrUpdateProvider(
        name: String,
        payload: OIDCCreateOrUpdateProviderPayload
    ): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "provider", name)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }
}
