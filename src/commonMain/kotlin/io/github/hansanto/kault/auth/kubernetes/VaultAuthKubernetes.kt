package io.github.hansanto.kault.auth.kubernetes

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.common.LoginResponse
import io.github.hansanto.kault.auth.kubernetes.payload.KubernetesConfigureAuthPayload
import io.github.hansanto.kault.auth.kubernetes.payload.KubernetesLoginPayload
import io.github.hansanto.kault.auth.kubernetes.payload.KubernetesWriteAuthRolePayload
import io.github.hansanto.kault.auth.kubernetes.response.KubernetesConfigureAuthResponse
import io.github.hansanto.kault.auth.kubernetes.response.KubernetesReadAuthRoleResponse
import io.github.hansanto.kault.extension.decodeBodyJsonFieldObject
import io.github.hansanto.kault.extension.list
import io.github.hansanto.kault.response.StandardListResponse
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
 * @see VaultAuthKubernetes.configure(payload)
 */
public suspend inline fun VaultAuthKubernetes.configure(
    payloadBuilder: BuilderDsl<KubernetesConfigureAuthPayload.Builder>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = KubernetesConfigureAuthPayload.Builder().apply(payloadBuilder).build()
    return configure(payload)
}

/**
 * @see VaultAuthKubernetes.createOrUpdateRole(roleName, payload)
 */
public suspend inline fun VaultAuthKubernetes.createOrUpdateRole(
    roleName: String,
    payloadBuilder: BuilderDsl<KubernetesWriteAuthRolePayload.Builder>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = KubernetesWriteAuthRolePayload.Builder().apply(payloadBuilder).build()
    return createOrUpdateRole(roleName, payload)
}

/**
 * @see VaultAuthKubernetes.login(payload)
 */
public suspend inline fun VaultAuthKubernetes.login(
    payloadBuilder: BuilderDsl<KubernetesLoginPayload.Builder>
): LoginResponse {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = KubernetesLoginPayload.Builder().apply(payloadBuilder).build()
    return login(payload)
}

/**
 * Provides methods for managing Kubernetes authentication within Vault.
 */
public interface VaultAuthKubernetes {

    /**
     * This endpoint configures the public key used to validate the JWT signature and the necessary information to access the Kubernetes API.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/kubernetes#configure-method)
     * @return Returns true if the configuration was updated successfully.
     */
    public suspend fun configure(payload: KubernetesConfigureAuthPayload): Boolean

    /**
     * Reads the current configuration for the Kubernetes auth method.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/kubernetes#read-config)
     * @return The previously configured config, excluding credentials.
     */
    public suspend fun readConfiguration(): KubernetesConfigureAuthResponse

    /**
     * Registers a role in the auth method.
     * Role types have specific entities that can perform login operations against this endpoint.
     * Constraints specific to the role type must be set on the role.
     * These are applied to the authenticated entities attempting to login.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/kubernetes#create-update-role)
     * @param roleName Name of the role
     * @param payload Optional parameters for creating or updating a role.
     * @return Returns true if the role was created or updated successfully.
     */
    public suspend fun createOrUpdateRole(
        roleName: String,
        payload: KubernetesWriteAuthRolePayload
    ): Boolean

    /**
     * Returns the previously registered role configuration.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/kubernetes#read-role)
     * @param roleName Name of the role.
     * @return Response.
     */
    public suspend fun readRole(roleName: String): KubernetesReadAuthRoleResponse

    /**
     * Lists all the roles that are registered with the auth method.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/kubernetes#list-roles)
     * @return List of role names.
     */
    public suspend fun list(): List<String>

    /**
     * Deletes the role configuration.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/kubernetes#delete-role)
     * @param roleName Name of the role.
     * @return Returns true if the role was deleted successfully.
     */
    public suspend fun deleteRole(roleName: String): Boolean

    /**
     * Fetch a token.
     * This endpoint takes a signed JSON Web Token (JWT) and a role name for some entity.
     * It verifies the JWT signature to authenticate that entity and then authorizes the entity for the given role.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/kubernetes#login)
     * @param payload Parameters to login with Kubernetes.
     * @return Response.
     */
    public suspend fun login(payload: KubernetesLoginPayload): LoginResponse
}

/**
 * Implementation of [VaultAuthKubernetes].
 */
public class VaultAuthKubernetesImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultAuthKubernetes {

    public companion object {

        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultAuthKubernetesImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "kubernetes"
    }

    /**
     * Builder class to simplify the creation of [VaultAuthKubernetesImpl].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder : ServiceBuilder<VaultAuthKubernetesImpl>() {

        public override var path: String = Default.PATH

        public override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultAuthKubernetesImpl =
            VaultAuthKubernetesImpl(
                client = client,
                path = completePath
            )
    }

    override suspend fun configure(payload: KubernetesConfigureAuthPayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "config")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun readConfiguration(): KubernetesConfigureAuthResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "config")
            }
        }
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun createOrUpdateRole(roleName: String, payload: KubernetesWriteAuthRolePayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "role", roleName)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun readRole(roleName: String): KubernetesReadAuthRoleResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "role", roleName)
            }
        }
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun list(): List<String> {
        val response = client.list {
            url {
                appendPathSegments(path, "role")
            }
        }
        return response.decodeBodyJsonFieldObject<StandardListResponse>("data", VaultClient.json).keys
    }

    override suspend fun deleteRole(roleName: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(path, "role", roleName)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun login(payload: KubernetesLoginPayload): LoginResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "login")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.decodeBodyJsonFieldObject("auth", VaultClient.json)
    }
}
