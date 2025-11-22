package io.github.hansanto.kault.identity.oidc

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.extension.decodeBodyJsonDataFieldObject
import io.github.hansanto.kault.extension.list
import io.github.hansanto.kault.identity.oidc.payload.OIDCCreateOrUpdateAssignmentPayload
import io.github.hansanto.kault.identity.oidc.payload.OIDCCreateOrUpdateClientPayload
import io.github.hansanto.kault.identity.oidc.payload.OIDCCreateOrUpdateProviderPayload
import io.github.hansanto.kault.identity.oidc.payload.OIDCCreateOrUpdateScopePayload
import io.github.hansanto.kault.identity.oidc.response.OIDCListClientsResponse
import io.github.hansanto.kault.identity.oidc.response.OIDCListProvidersResponse
import io.github.hansanto.kault.identity.oidc.response.OIDCReadAssignmentResponse
import io.github.hansanto.kault.identity.oidc.response.OIDCReadClientResponse
import io.github.hansanto.kault.identity.oidc.response.OIDCReadProviderResponse
import io.github.hansanto.kault.identity.oidc.response.OIDCReadScopeResponse
import io.github.hansanto.kault.response.StandardListResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.parameter
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

/**
 * @see VaultIdentityOIDC.createOrUpdateScope(name, payload)
 */
public suspend inline fun VaultIdentityOIDC.createOrUpdateScope(
    name: String,
    builder: BuilderDsl<OIDCCreateOrUpdateScopePayload.Builder>
): Boolean {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    val payload = OIDCCreateOrUpdateScopePayload.Builder().apply(builder).build()
    return createOrUpdateScope(name, payload)
}

/**
 * @see VaultIdentityOIDC.createOrUpdateClient(name, payload)
 */
public suspend fun VaultIdentityOIDC.createOrUpdateClient(
    name: String,
    builder: BuilderDsl<OIDCCreateOrUpdateClientPayload>
): Boolean {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    val payload = OIDCCreateOrUpdateClientPayload().apply(builder)
    return createOrUpdateClient(name, payload)
}

/**
 * @see VaultIdentityOIDC.createOrUpdateAssignment(name, payload)
 */
public suspend inline fun VaultIdentityOIDC.createOrUpdateAssignment(
    name: String,
    builder: BuilderDsl<OIDCCreateOrUpdateAssignmentPayload>
): Boolean {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    val payload = OIDCCreateOrUpdateAssignmentPayload().apply(builder)
    return createOrUpdateAssignment(name, payload)
}

public interface VaultIdentityOIDC {

    /**
     * This endpoint creates or updates a Provider.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#create-or-update-a-provider)
     * @param name The name of the provider.
     * @param payload The configuration payload for the OIDC provider.
     * @return Returns true if the configuration was updated successfully.
     */
    public suspend fun createOrUpdateProvider(name: String, payload: OIDCCreateOrUpdateProviderPayload = OIDCCreateOrUpdateProviderPayload()): Boolean

    /**
     * This endpoint queries the OIDC provider by its name.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#read-provider-by-name)
     * @param name The name of the provider.
     * @return Returns the provider information.
     */
    public suspend fun readProvider(name: String): OIDCReadProviderResponse

    /**
     * This endpoint returns a list of all OIDC providers.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#list-providers)
     * @param allowedClientId Filters the list of OIDC providers to those that allow the given client ID in their set of [allowed_client_ids](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#allowed_client_ids).
     * @return The list of providers.
     */
    public suspend fun listProviders(allowedClientId: String? = null): OIDCListProvidersResponse

    /**
     * This endpoint deletes an OIDC provider.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#delete-provider-by-name)
     * @param name The name of the provider.
     * @return `true` if the provider was deleted successfully, `false` otherwise.
     */
    public suspend fun deleteProvider(name: String): Boolean

    /**
     * This endpoint creates or updates a scope.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#create-or-update-a-scope)
     * @param name The name of the scope. This parameter is specified as part of the URL. The openid scope name is reserved.
     * @param payload The configuration payload for the OIDC scope.
     */
    public suspend fun createOrUpdateScope(
        name: String,
        payload: OIDCCreateOrUpdateScopePayload = OIDCCreateOrUpdateScopePayload()
    ): Boolean

    /**
     * This endpoint queries a scope by its name.
     * [Documentation](This endpoint queries a scope by its name.)
     * @param name The name of the scope.
     * @return The scope information.
     */
    public suspend fun readScope(name: String): OIDCReadScopeResponse

    /**
     * This endpoint returns a list of all configured scopes.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#list-scopes)
     * @return The list of scope names.
     */
    public suspend fun listScopes(): List<String>

    /**
     * This endpoint deletes a scope.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#delete-scope-by-name)
     * @param name The name of the scope.
     * @return `true` if the scope was deleted successfully, `false` otherwise.
     */
    public suspend fun deleteScope(name: String): Boolean

    /**
     * This endpoint creates or updates a client.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#create-or-update-a-client)
     * @param name The name of the client. This parameter is specified as part of the URL.
     * @param payload The configuration payload for the OIDC client.
     * @return `true` if the client was created or updated successfully, `false` otherwise.
     */
    public suspend fun createOrUpdateClient(
        name: String,
        payload: OIDCCreateOrUpdateClientPayload = OIDCCreateOrUpdateClientPayload()
    ): Boolean

    /**
     * This endpoint queries a client by its name.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#read-client-by-name)
     * @param name The name of the client.
     * @return The client information.
     */
    public suspend fun readClient(name: String): OIDCReadClientResponse

    /**
     * This endpoint returns a list of all configured clients.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#list-clients)
     * @return The list of clients.
     */
    public suspend fun listClients(): OIDCListClientsResponse

    /**
     * This endpoint deletes a client.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#delete-client-by-name)
     * @param name The name of the client.
     * @return `true` if the client was deleted successfully, `false` otherwise.
     */
    public suspend fun deleteClient(name: String): Boolean

    /**
     * This endpoint creates or updates an assignment.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#create-or-update-an-assignment)
     * @param name The name of the assignment.
     * @param payload The configuration payload for the OIDC assignment.
     * @return `true` if the assignment was created or updated successfully, `false` otherwise.
     */
    public suspend fun createOrUpdateAssignment(name: String, payload: OIDCCreateOrUpdateAssignmentPayload = OIDCCreateOrUpdateAssignmentPayload()): Boolean

    /**
     * This endpoint queries an assignment by its name.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#read-assignment-by-name)
     * @param name The name of the assignment.
     * @return The assignment information.
     */
    public suspend fun readAssignment(name: String) : OIDCReadAssignmentResponse

    /**
     * This endpoint returns a list of all configured assignments.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#list-assignments)
     * @return The list of assignment names.
     */
    public suspend fun listAssignments() : List<String>

    /**
     * This endpoint deletes an assignment.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider#delete-assignment-by-name)
     * @param name The name of the assignment.
     * @return `true` if the assignment was deleted successfully, `false` otherwise.
     */
    public suspend fun deleteAssignment(name: String) : Boolean
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

    override suspend fun readProvider(name: String): OIDCReadProviderResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "provider", name)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun listProviders(allowedClientId: String?): OIDCListProvidersResponse {
        val response = client.list {
            url {
                appendPathSegments(path, "provider")
                parameter("allowed_client_id", allowedClientId)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun deleteProvider(name: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(path, "provider", name)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun createOrUpdateScope(
        name: String,
        payload: OIDCCreateOrUpdateScopePayload
    ): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "scope", name)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun readScope(name: String): OIDCReadScopeResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "scope", name)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun listScopes(): List<String> {
        val response = client.list {
            url {
                appendPathSegments(path, "scope")
            }
        }
        return response.decodeBodyJsonDataFieldObject<StandardListResponse>().keys
    }

    override suspend fun deleteScope(name: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(path, "scope", name)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun createOrUpdateClient(
        name: String,
        payload: OIDCCreateOrUpdateClientPayload
    ): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "client", name)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun readClient(name: String): OIDCReadClientResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "client", name)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun listClients(): OIDCListClientsResponse {
        val response = client.list {
            url {
                appendPathSegments(path, "client")
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun deleteClient(name: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(path, "client", name)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun createOrUpdateAssignment(
        name: String,
        payload: OIDCCreateOrUpdateAssignmentPayload
    ): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "assignment", name)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun readAssignment(name: String): OIDCReadAssignmentResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "assignment", name)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun listAssignments(): List<String> {
        val response = client.list {
            url {
                appendPathSegments(path, "assignment")
            }
        }
        return response.decodeBodyJsonDataFieldObject<StandardListResponse>().keys
    }

    override suspend fun deleteAssignment(name: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(path, "assignment", name)
            }
        }
        return response.status.isSuccess()
    }
}
