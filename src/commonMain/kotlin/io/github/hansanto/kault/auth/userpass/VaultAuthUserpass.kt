package io.github.hansanto.kault.auth.userpass

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.VaultAuth
import io.github.hansanto.kault.auth.userpass.payload.UserpassWriteUserPayload
import io.github.hansanto.kault.auth.userpass.response.ReadUserResponse
import io.github.hansanto.kault.extension.decodeBodyJsonDataFieldObject
import io.ktor.client.HttpClient
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
    public suspend fun createOrUpdate(
        username: String,
        payload: UserpassWriteUserPayload
    ): Boolean

    /**
     * Reads the properties of an existing username.
     * @param username The username for the user.
     * @return UserpassReadUserPayload
     */
    public suspend fun read(username: String): ReadUserResponse
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
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder : ServiceBuilder<VaultAuthUserpassImpl>() {

        public override var path: String = Default.PATH

        public override fun buildWithFullPath(client: HttpClient, fullPath: String): VaultAuthUserpassImpl =
            VaultAuthUserpassImpl(
                client = client,
                path = fullPath
            )
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

    override suspend fun read(username: String): ReadUserResponse {
        val response = client.get {
            url {
                appendPathSegments(path, "users", username)
            }
            contentType(ContentType.Application.Json)
        }
        return response.decodeBodyJsonDataFieldObject()
    }
}
