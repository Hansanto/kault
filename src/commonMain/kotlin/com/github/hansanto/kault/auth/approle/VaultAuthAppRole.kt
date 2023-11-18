package com.github.hansanto.kault.auth.approle

import com.github.hansanto.kault.BuilderDsl
import com.github.hansanto.kault.ServiceBuilder
import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.auth.VaultAuth
import com.github.hansanto.kault.auth.approle.payload.CreateCustomSecretIDPayload
import com.github.hansanto.kault.auth.approle.payload.CreateOrUpdatePayload
import com.github.hansanto.kault.auth.approle.payload.GenerateSecretIDPayload
import com.github.hansanto.kault.auth.approle.payload.LoginPayload
import com.github.hansanto.kault.auth.approle.payload.RoleIdPayload
import com.github.hansanto.kault.auth.approle.payload.SecretIdAccessorPayload
import com.github.hansanto.kault.auth.approle.payload.SecretIdPayload
import com.github.hansanto.kault.auth.approle.response.LoginResponse
import com.github.hansanto.kault.auth.approle.response.LookUpSecretIdResponse
import com.github.hansanto.kault.auth.approle.response.ReadRoleIdResponse
import com.github.hansanto.kault.auth.approle.response.ReadRoleResponse
import com.github.hansanto.kault.auth.approle.response.WriteSecretIdResponse
import com.github.hansanto.kault.extension.decodeBodyJsonFieldArray
import com.github.hansanto.kault.extension.decodeBodyJsonFieldObject
import com.github.hansanto.kault.extension.decodeBodyJsonFieldObjectOrNull
import com.github.hansanto.kault.response.StandardListResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.request
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @see VaultAuthAppRole.createOrUpdate(roleName, payload)
 */
public suspend inline fun VaultAuthAppRole.createOrUpdate(
    roleName: String,
    payloadBuilder: BuilderDsl<CreateOrUpdatePayload>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = CreateOrUpdatePayload().apply(payloadBuilder)
    return createOrUpdate(roleName, payload)
}

/**
 * @see VaultAuthAppRole.generateSecretID(roleName, payload)
 */
public suspend inline fun VaultAuthAppRole.generateSecretID(
    roleName: String,
    payloadBuilder: BuilderDsl<GenerateSecretIDPayload>
): WriteSecretIdResponse {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = GenerateSecretIDPayload().apply(payloadBuilder)
    return generateSecretID(roleName, payload)
}

/**
 * @see VaultAuthAppRole.createCustomSecretID(roleName, payload)
 */
public suspend inline fun VaultAuthAppRole.createCustomSecretID(
    roleName: String,
    payloadBuilder: BuilderDsl<CreateCustomSecretIDPayload.Builder>
): WriteSecretIdResponse {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = CreateCustomSecretIDPayload.Builder().apply(payloadBuilder).build()
    return createCustomSecretID(roleName, payload)
}

/**
 * @see VaultAuthAppRole.login(payload)
 */
public suspend inline fun VaultAuthAppRole.login(
    payloadBuilder: BuilderDsl<LoginPayload.Builder>
): LoginResponse {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = LoginPayload.Builder().apply(payloadBuilder).build()
    return login(payload)
}

/**
 * Provides methods for managing AppRole authentication within Vault.
 */
public interface VaultAuthAppRole {

    /**
     * This endpoint returns a list the existing AppRoles in the method.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#list-roles)
     * @return List of AppRoles.
     */
    public suspend fun list(): List<String>

    /**
     * Creates a new AppRole or updates an existing AppRole. This endpoint supports both create and update capabilities. There can be one or more constraints enabled on the role. It is required to have at least one of them enabled while creating or updating a role.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#create-update-approle)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes, accepted characters include a-Z, 0-9, space, hyphen, underscore and periods.
     * @param payload Optional parameters for creating or updating an AppRole.
     * @return Returns true if the AppRole was created or updated successfully.
     */
    public suspend fun createOrUpdate(
        roleName: String,
        payload: CreateOrUpdatePayload = CreateOrUpdatePayload()
    ): Boolean

    /**
     * Reads the properties of an existing AppRole.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#read-approle)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes.
     * @return Response.
     */
    public suspend fun read(roleName: String): ReadRoleResponse

    /**
     * Deletes an existing AppRole from the method.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#delete-approle)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes.
     * @return Returns true if the AppRole was deleted successfully.
     */
    public suspend fun delete(roleName: String): Boolean

    /**
     * Reads the RoleID of an existing AppRole.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#read-approle-role-id)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes.
     * @return Response.
     */
    public suspend fun readRoleID(roleName: String): ReadRoleIdResponse

    /**
     * Updates the RoleID of an existing AppRole to a custom value.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#update-approle-role-id)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes.
     * @param roleId Value to be set as RoleID.
     * @return Returns true if the RoleID was updated successfully.
     */
    public suspend fun updateRoleID(roleName: String, roleId: String): Boolean

    /**
     * Generates and issues a new SecretID on an existing AppRole. Similar to tokens, the response will also contain a secret_id_accessor value which can be used to read the properties of the SecretID without divulging the SecretID itself, and also to delete the SecretID from the AppRole.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#generate-new-secret-id)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes.
     * @param payload Parameters for generating a SecretID.
     * @return Response.
     */
    public suspend fun generateSecretID(
        roleName: String,
        payload: GenerateSecretIDPayload = GenerateSecretIDPayload()
    ): WriteSecretIdResponse

    /**
     * Lists the accessors of all the SecretIDs issued against the AppRole. This includes the accessors for "custom" SecretIDs as well.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#list-secret-id-accessors)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes.
     * @return List of SecretID accessors.
     */
    public suspend fun secretIdAccessors(roleName: String): List<String>

    /**
     * Reads out the properties of a SecretID.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#read-approle-secret-id)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes.
     * @param secretId Secret ID attached to the role.
     * @return Response.
     */
    public suspend fun readSecretID(roleName: String, secretId: String): LookUpSecretIdResponse?

    /**
     * Destroy an AppRole secret ID.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#destroy-approle-secret-id)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes.
     * @param secretId Secret ID attached to the role.
     * @return Returns true if the SecretID was destroyed successfully.
     */
    public suspend fun destroySecretID(roleName: String, secretId: String): Boolean

    /**
     * Reads out the properties of a SecretID.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#read-approle-secret-id-accessor)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes.
     * @param secretIdAccessor Secret ID accessor attached to the role.
     * @return Response.
     */
    public suspend fun readSecretIDAccessor(roleName: String, secretIdAccessor: String): LookUpSecretIdResponse

    /**
     * Destroy an AppRole secret ID by its accessor.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#destroy-approle-secret-id-accessor)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes.
     * @param secretIdAccessor Secret ID accessor attached to the role.
     * @return Returns true if the SecretID was destroyed successfully.
     */
    public suspend fun destroySecretIDAccessor(roleName: String, secretIdAccessor: String): Boolean

    /**
     * Assigns a "custom" SecretID against an existing AppRole. This is used in the "Push" model of operation.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#create-custom-approle-secret-id)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes.
     * @param payload Parameters for generating a SecretID.
     * @return Response.
     */
    public suspend fun createCustomSecretID(
        roleName: String,
        payload: CreateCustomSecretIDPayload
    ): WriteSecretIdResponse

    /**
     * Issues a Vault token based on the presented credentials. Role_id is always required; if bind_secret_id is enabled (the default) on the AppRole, secret_id is required too. Any other bound authentication values on the AppRole (such as client IP CIDR) are also evaluated.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#login-with-approle)
     * @param payload Parameters to login with AppRole.
     * @return Response.
     */
    public suspend fun login(payload: LoginPayload): LoginResponse

    /**
     * Performs some maintenance tasks to clean up invalid entries that may remain in the token store. Generally, running this is not needed unless upgrade notes or support personnel suggest it. This may perform a lot of I/O to the storage method so should be used sparingly.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#tidy-tokens)
     * @return List of alerts.
     */
    public suspend fun tidyTokens(): List<String>
}

/**
 * Implementation of [VaultAuthAppRole].
 */
public class VaultAuthAppRoleImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultAuthAppRole {

    public companion object {

        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultAuthAppRoleImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "approle"
    }

    /**
     * Builder class to simplify the creation of [VaultAuth].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder : ServiceBuilder<VaultAuthAppRoleImpl>() {

        public override var path: String = Default.PATH

        public override fun buildWithFullPath(client: HttpClient, fullPath: String): VaultAuthAppRoleImpl =
            VaultAuthAppRoleImpl(
                client = client,
                path = fullPath
            )
    }

    override suspend fun list(): List<String> {
        val response = client.request {
            method = HttpMethod("LIST")
            url {
                appendPathSegments(path, "role")
            }
        }
        return response.decodeBodyJsonFieldObject<StandardListResponse>("data", VaultClient.json).keys
    }

    override suspend fun createOrUpdate(roleName: String, payload: CreateOrUpdatePayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "role", roleName)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun read(roleName: String): ReadRoleResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "role", roleName)
            }
        }
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun delete(roleName: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(path, "role", roleName)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun readRoleID(roleName: String): ReadRoleIdResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "role", roleName, "role-id")
            }
        }
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun updateRoleID(roleName: String, roleId: String): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "role", roleName, "role-id")
            }
            contentType(ContentType.Application.Json)
            setBody(RoleIdPayload(roleId))
        }
        return response.status.isSuccess()
    }

    override suspend fun generateSecretID(
        roleName: String,
        payload: GenerateSecretIDPayload
    ): WriteSecretIdResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "role", roleName, "secret-id")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun secretIdAccessors(roleName: String): List<String> {
        val response = client.request {
            method = HttpMethod("LIST")
            url {
                appendPathSegments(path, "role", roleName, "secret-id")
            }
        }
        return response.decodeBodyJsonFieldObject<StandardListResponse>("data", VaultClient.json).keys
    }

    override suspend fun readSecretID(roleName: String, secretId: String): LookUpSecretIdResponse? {
        val response = client.post {
            url {
                appendPathSegments(path, "role", roleName, "secret-id", "lookup")
            }
            contentType(ContentType.Application.Json)
            setBody(SecretIdPayload(secretId))
        }

        // After destroying a secret ID, Vault returns an empty body
        return response.decodeBodyJsonFieldObjectOrNull("data", VaultClient.json)
    }

    override suspend fun destroySecretID(roleName: String, secretId: String): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "role", roleName, "secret-id", "destroy")
            }
            contentType(ContentType.Application.Json)
            setBody(SecretIdPayload(secretId))
        }

        return response.status.isSuccess()
    }

    override suspend fun readSecretIDAccessor(
        roleName: String,
        secretIdAccessor: String
    ): LookUpSecretIdResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "role", roleName, "secret-id-accessor", "lookup")
            }
            contentType(ContentType.Application.Json)
            setBody(SecretIdAccessorPayload(secretIdAccessor))
        }

        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun destroySecretIDAccessor(roleName: String, secretIdAccessor: String): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "role", roleName, "secret-id-accessor", "destroy")
            }
            contentType(ContentType.Application.Json)
            setBody(SecretIdAccessorPayload(secretIdAccessor))
        }
        return response.status.isSuccess()
    }

    override suspend fun createCustomSecretID(
        roleName: String,
        payload: CreateCustomSecretIDPayload
    ): WriteSecretIdResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "role", roleName, "custom-secret-id")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.decodeBodyJsonFieldObject("data", VaultClient.json)
    }

    override suspend fun login(payload: LoginPayload): LoginResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "login")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.decodeBodyJsonFieldObject("auth", VaultClient.json)
    }

    override suspend fun tidyTokens(): List<String> {
        val response = client.post {
            url {
                appendPathSegments(path, "tidy", "secret-id")
            }
        }
        return response.decodeBodyJsonFieldArray("warnings", VaultClient.json)
    }
}
