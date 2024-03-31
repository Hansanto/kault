package io.github.hansanto.kault.auth.userpass

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.VaultAuth
import io.github.hansanto.kault.auth.common.response.LoginResponse
import io.github.hansanto.kault.auth.userpass.payload.UserpassLoginRequest
import io.github.hansanto.kault.auth.userpass.payload.UserpassResetPasswordPayload
import io.github.hansanto.kault.auth.userpass.payload.UserpassResetPoliciesPayload
import io.github.hansanto.kault.auth.userpass.payload.UserpassWriteUserPayload
import io.github.hansanto.kault.auth.userpass.response.UserpassReadUserResponse
import io.github.hansanto.kault.extension.decodeBodyJsonAuthFieldObject
import io.github.hansanto.kault.extension.decodeBodyJsonDataFieldObject
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
 * @see VaultAuthUserpass.createOrUpdate(username, payload)
 */
public suspend inline fun VaultAuthUserpass.createOrUpdate(
    username: String,
    payloadBuilder: BuilderDsl<UserpassWriteUserPayload.Builder>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = UserpassWriteUserPayload.Builder().apply(payloadBuilder).build()
    return createOrUpdate(username, payload)
}

/**
 * Provides methods for managing Username & Password authentication within Vault.
 */
public interface VaultAuthUserpass {

    /**
     * Create a new user or update an existing user. This path honors the distinction between the create and update capabilities inside ACL policies.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/userpass#create-update-user)
     * @param username The username for the user. Accepted characters: alphanumeric plus "_", "-", "." (underscore, hyphen and period); username cannot begin with a hyphen, nor can it begin or end with a period.
     * @param payload Optional parameters for creating or updating an user.
     * @return Returns true if the user was created or updated successfully.
     */
    public suspend fun createOrUpdate(username: String, payload: UserpassWriteUserPayload): Boolean

    /**
     * Reads the properties of an existing username.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/userpass#read-user)
     * @param username The username for the user.
     * @return Response.
     */
    public suspend fun read(username: String): UserpassReadUserResponse

    /**
     * This endpoint deletes the user from the method.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/userpass#delete-user)
     * @param username The username for the user.
     * @return Returns true if the user was deleted successfully.
     */
    public suspend fun delete(username: String): Boolean

    /**
     * Update password for an existing user.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/userpass#update-password-on-user)
     * @param username The username for the user.
     * @param password The password for the user.
     * @return Returns true if the password was updated successfully.
     */
    public suspend fun updatePassword(username: String, password: String): Boolean

    /**
     * Update policies for an existing user.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/userpass#update-policies-on-user)
     * @param username The username for the user.
     * @param tokenPolicies List of policies to encode onto generated tokens. Depending on the auth method, this list may be supplemented by user/group/other values.
     * @return Returns true if the policies were updated successfully.
     */
    public suspend fun updatePolicies(username: String, tokenPolicies: List<String>): Boolean

    /**
     * List available userpass users.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/userpass#list-users)
     * @return Returns a list of usernames.
     */
    public suspend fun list(): List<String>

    /**
     * Login with the username and password.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/auth/userpass#login)
     * @param username The username for the user.
     * @param password The password for the user.
     * @return Returns the token information.
     */
    public suspend fun login(username: String, password: String): LoginResponse
}

/**
 * Implementation of [VaultAuthUserpass].
 */
public class VaultAuthUserpassImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultAuthUserpass {

    public companion object {

        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultAuthUserpassImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "userpass"
    }

    /**
     * Builder class to simplify the creation of [VaultAuth].
     */
    public class Builder : ServiceBuilder<VaultAuthUserpassImpl>() {

        public override var path: String = Default.PATH

        override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultAuthUserpassImpl {
            return VaultAuthUserpassImpl(
                client = client,
                path = completePath
            )
        }
    }

    override suspend fun createOrUpdate(username: String, payload: UserpassWriteUserPayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "users", username)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun read(username: String): UserpassReadUserResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "users", username)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun delete(username: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(path, "users", username)
            }
        }
        return response.status.isSuccess()
    }

    override suspend fun updatePassword(username: String, password: String): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "users", username, "password")
            }
            contentType(ContentType.Application.Json)
            setBody(UserpassResetPasswordPayload(password))
        }
        return response.status.isSuccess()
    }

    override suspend fun updatePolicies(username: String, tokenPolicies: List<String>): Boolean {
        val response = client.post {
            url {
                appendPathSegments(path, "users", username, "policies")
            }
            contentType(ContentType.Application.Json)
            setBody(UserpassResetPoliciesPayload(tokenPolicies))
        }
        return response.status.isSuccess()
    }

    override suspend fun list(): List<String> {
        val response = client.list {
            url {
                appendPathSegments(path, "users")
            }
        }
        return response.decodeBodyJsonDataFieldObject<StandardListResponse>().keys
    }

    override suspend fun login(username: String, password: String): LoginResponse {
        val response = client.post {
            url {
                appendPathSegments(path, "login", username)
            }
            contentType(ContentType.Application.Json)
            setBody(UserpassLoginRequest(password))
        }
        return response.decodeBodyJsonAuthFieldObject()
    }
}
