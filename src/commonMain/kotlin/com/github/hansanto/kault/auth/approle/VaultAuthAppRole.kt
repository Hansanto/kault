package com.github.hansanto.kault.auth.approle

import com.github.hansanto.kault.auth.approle.payload.CreateOrUpdatePayload
import com.github.hansanto.kault.auth.approle.payload.GenerateSecretIDPayload
import com.github.hansanto.kault.auth.approle.payload.LoginPayload
import com.github.hansanto.kault.auth.approle.payload.RoleIdPayload
import com.github.hansanto.kault.auth.approle.payload.SecretIdPayload
import io.ktor.client.HttpClient
import io.ktor.client.call.body
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
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @see VaultAuthAppRole.createOrUpdate(roleName, payload)
 */
public suspend inline fun VaultAuthAppRole.createOrUpdate(
    roleName: String,
    payloadBuilder: CreateOrUpdatePayload.() -> Unit
): Any {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = CreateOrUpdatePayload().apply(payloadBuilder)
    return createOrUpdate(roleName, payload)
}

/**
 * @see VaultAuthAppRole.generateSecretID(roleName, payload)
 */
public suspend inline fun VaultAuthAppRole.generateSecretID(
    roleName: String,
    payloadBuilder: GenerateSecretIDPayload.() -> Unit
): Any {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = GenerateSecretIDPayload().apply(payloadBuilder)
    return generateSecretID(roleName, payload)
}

public interface VaultAuthAppRole {

    /**
     * Creates a new AppRole or updates an existing AppRole. This endpoint supports both create and update capabilities. There can be one or more constraints enabled on the role. It is required to have at least one of them enabled while creating or updating a role.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/approle#create-update-approle)
     * @param roleName Name of the AppRole. Must be less than 4096 bytes, accepted characters include a-Z, 0-9, space, hyphen, underscore and periods.
     * @param payload Optional parameters for creating or updating an AppRole.
     * @return Returns true if the AppRole was created or updated successfully.
     */
    public suspend fun createOrUpdate(roleName: String, payload: CreateOrUpdatePayload = CreateOrUpdatePayload()): Boolean

    public suspend fun read(roleName: String): Any

    public suspend fun delete(roleName: String): Boolean

    public suspend fun readRoleID(roleName: String): String

    public suspend fun updateRoleID(roleName: String, roleId: String): Boolean

    public suspend fun generateSecretID(roleName: String, payload: GenerateSecretIDPayload = GenerateSecretIDPayload()): Any

    public suspend fun secretIdAccessors(roleName: String): List<String>

    public suspend fun readSecretID(roleName: String, secretId: String): Any

    public suspend fun destroySecretID(roleName: String, secretId: String): Boolean

    public suspend fun login(roleId: String, secretId: String): Any
}

public class VaultAuthAppRoleImpl(
    private val client: HttpClient,
    public val path: String = "auth/approle"
) : VaultAuthAppRole {

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

    override suspend fun read(roleName: String): Any {
        val response = client.get {
            url {
                appendPathSegments(path, "role", roleName)
            }
        }
        return response.body()
    }

    override suspend fun delete(roleName: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(path, "role", roleName)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun readRoleID(roleName: String): String {
        val response = client.get {
            url {
                appendPathSegments(path, "role", roleName, "role-id")
            }
        }
        return response.body<JsonElement>().jsonObject["data"]!!.jsonObject["role_id"]!!.jsonPrimitive.content
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

    override suspend fun generateSecretID(roleName: String, payload: GenerateSecretIDPayload): Any {
        val response = client.post {
            url {
                appendPathSegments(path, "role", roleName, "secret-id")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.body()
    }

    override suspend fun secretIdAccessors(roleName: String): List<String> {
        val response = client.request {
            method = HttpMethod("LIST")
            url {
                appendPathSegments(path, "role", roleName, "secret-id")
            }
        }
        return emptyList()
    }

    override suspend fun readSecretID(roleName: String, secretId: String): Any {
        val response = client.post {
            url {
                appendPathSegments(path, "role", roleName, "secret-id", "lookup")
            }
            contentType(ContentType.Application.Json)
            setBody(SecretIdPayload(secretId))
        }
        return response.body()
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

    override suspend fun login(roleId: String, secretId: String): Any {
        val payload = LoginPayload(roleId, secretId)
        client.post {
            url {
                appendPathSegments(path, "login")
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return Unit
    }
}
