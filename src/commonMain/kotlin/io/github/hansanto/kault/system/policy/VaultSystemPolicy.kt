package io.github.hansanto.kault.system.policy

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.extension.decodeBodyJsonDataFieldObject
import io.github.hansanto.kault.extension.list
import io.github.hansanto.kault.system.policy.payload.PolicyCreateOrUpdatePayload
import io.github.hansanto.kault.system.policy.response.PolicyListResponse
import io.github.hansanto.kault.system.policy.response.PolicyReadResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.appendPathSegments
import io.ktor.http.contentType
import io.ktor.http.isSuccess

public interface VaultSystemPolicy {

    /**
     * This endpoint lists all configured policies.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/policy#list-policies)
     * @return Response containing the list of policies.
     */
    public suspend fun list(): PolicyListResponse

    /**
     * This endpoint retrieve the policy body for the named policy.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/policy#read-policy)
     * @param name Specifies the name of the policy to retrieve. This is specified as part of the request URL.
     * @return Information about the policy.
     */
    public suspend fun read(name: String): PolicyReadResponse

    /**
     * This endpoint adds a new or updates an existing policy. Once a policy is updated, it takes effect immediately to all associated users.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/policy#create-update-policy)
     * @param name Specifies the name of the policy to create. This is specified as part of the request URL.
     * @param policy Specifies the policy document.
     * @return `true` if the policy was created or updated successfully, `false` otherwise.
     */
    public suspend fun createOrUpdate(name: String, policy: String): Boolean

    /**
     * This endpoint deletes the policy with the given name. This will immediately affect all users associated with this policy.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/policy#delete-policy)
     * @param name Specifies the name of the policy to delete. This is specified as part of the request URL.
     * @return `true` if the policy was deleted successfully, `false` otherwise.
     */
    public suspend fun delete(name: String): Boolean
}

/**
 * Implementation of [VaultSystemPolicy].
 */
public class VaultSystemPolicyImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultSystemPolicy {

    public companion object {

        /**
         * Creates a new instance of [VaultSystemPolicyImpl] using the provided HttpClient and optional parent path.
         * @param client HttpClient to interact with API.
         * @param parentPath The optional parent path used for building the final path used to interact with endpoints.
         * @param builder Builder to define the authentication service.
         * @return The instance of [VaultSystemPolicyImpl] that was built.
         */
        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultSystemPolicyImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "policy"
    }

    /**
     * Builder class to simplify the creation of [VaultSystemPolicyImpl].
     */
    public open class Builder : ServiceBuilder<VaultSystemPolicyImpl>() {

        public override var path: String = Default.PATH

        override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultSystemPolicyImpl =
            VaultSystemPolicyImpl(
                client = client,
                path = completePath
            )
    }

    override suspend fun list(): PolicyListResponse {
        val response = client.list {
            url {
                appendPathSegments(this@VaultSystemPolicyImpl.path)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun read(name: String): PolicyReadResponse {
        val response = client.get {
            url {
                appendPathSegments(this@VaultSystemPolicyImpl.path, name)
            }
        }
        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun createOrUpdate(name: String, policy: String): Boolean {
        val payload = PolicyCreateOrUpdatePayload(policy = policy)
        val response = client.post {
            url {
                appendPathSegments(this@VaultSystemPolicyImpl.path, name)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun delete(name: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(this@VaultSystemPolicyImpl.path, name)
            }
        }
        return response.status.isSuccess()
    }
}
