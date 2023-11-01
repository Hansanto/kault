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
import io.ktor.http.HttpMethod
import io.ktor.http.appendPathSegments
import io.ktor.http.isSuccess
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

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
    public suspend fun createOrUpdate(roleName: String, payload: CreateOrUpdatePayload): Boolean

    public suspend fun read(roleName: String): Any

    public suspend fun delete(roleName: String): Boolean

    public suspend fun readRoleID(roleName: String): String

    public suspend fun updateRoleID(roleName: String, roleId: String): Boolean

    public suspend fun generateSecretID(roleName: String, payload: GenerateSecretIDPayload): Any

    public suspend fun secretIdAccessors(roleName: String): List<String>

    public suspend fun readSecretID(roleName: String, secretId: String): Any

    public suspend fun destroySecretID(roleName: String, secretId: String): Boolean

    public suspend fun login(roleId: String, secretId: String): Any
}

public class VaultAuthAppRoleImpl(
    private val client: HttpClient
) : VaultAuthAppRole {

    public companion object {
        private const val PATH: String = "auth/approle"
    }

    override suspend fun createOrUpdate(roleName: String, payload: CreateOrUpdatePayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(PATH, "role", roleName)
            }
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun read(roleName: String): Any {
        val response = client.get {
            url {
                appendPathSegments(PATH, "role", roleName)
            }
        }
        return response.body()
    }

    override suspend fun delete(roleName: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(PATH, "role", roleName)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun readRoleID(roleName: String): String {
        val response = client.get {
            url {
                appendPathSegments(PATH, "role", roleName, "role-id")
            }
        }
        return response.body<JsonElement>().jsonObject["data"]!!.jsonObject["role_id"]!!.jsonPrimitive.content
    }

    override suspend fun updateRoleID(roleName: String, roleId: String): Boolean {
        val response = client.post {
            url {
                appendPathSegments(PATH, "role", roleName, "role-id")
            }
            setBody(RoleIdPayload(roleId))
        }
        return response.status.isSuccess()
    }

    override suspend fun generateSecretID(roleName: String, payload: GenerateSecretIDPayload): Any {
        val response = client.post {
            url {
                appendPathSegments(PATH, "role", roleName, "secret-id")
            }
            setBody(payload)
        }
        return response.body()
    }

    override suspend fun secretIdAccessors(roleName: String): List<String> {
        val response = client.request {
            method = HttpMethod("LIST")
            url {
                appendPathSegments(PATH, "role", roleName, "secret-id")
            }
        }
        return emptyList()
    }

    override suspend fun readSecretID(roleName: String, secretId: String): Any {
        val response = client.post {
            url {
                appendPathSegments(PATH, "role", roleName, "secret-id", "lookup")
            }
            setBody(SecretIdPayload(secretId))
        }
        return response.body()
    }

    override suspend fun destroySecretID(roleName: String, secretId: String): Boolean {
        val response = client.post {
            url {
                appendPathSegments(PATH, "role", roleName, "secret-id", "destroy")
            }
            setBody(SecretIdPayload(secretId))
        }
        return response.status.isSuccess()
    }

    override suspend fun login(roleId: String, secretId: String): Any {
        val payload = LoginPayload(roleId, secretId)
        client.post {
            url {
                appendPathSegments(PATH, "login")
            }
            setBody(payload)
        }
        return Unit
    }
}
