package io.github.hansanto.kault.system.audit

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.extension.decodeBodyJsonDataFieldObject
import io.github.hansanto.kault.system.audit.payload.AuditingEnableDevicePayload
import io.github.hansanto.kault.system.audit.response.AuditingDeviceResponse
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
 * @see VaultSystemAudit.enable(path, payload)
 */
public suspend inline fun VaultSystemAudit.enable(
    roleName: String,
    payloadBuilder: BuilderDsl<AuditingEnableDevicePayload.Builder>
): Boolean {
    contract { callsInPlace(payloadBuilder, InvocationKind.EXACTLY_ONCE) }
    val payload = AuditingEnableDevicePayload.Builder().apply(payloadBuilder).build()
    return enable(roleName, payload)
}

/**
 * Provides methods for managing audit within Vault.
 */
public interface VaultSystemAudit {

    /**
     * This endpoint lists only the enabled audit devices (it does not list all available audit devices).
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/audit#list-enabled-audit-devices)
     * @return Returns a map of audit devices with the key being the path of the audit device.
     */
    public suspend fun list(): Map<String, AuditingDeviceResponse>

    /**
     * This endpoint enables a new audit device at the supplied path. The path can be a single word name or a more complex, nested path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/audit#enable-audit-device)
     * @param path Specifies the path in which to enable the audit device. This is part of the request URL.
     * @param payload Specifies the configuration of the audit device. This is part of the request body.
     * @return Returns true if the audit device was successfully enabled.
     */
    public suspend fun enable(path: String, payload: AuditingEnableDevicePayload): Boolean

    /**
     * This endpoint disables the audit device at the given path.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/system/audit#disable-audit-device)
     * @param path Specifies the path of the audit device to delete. This is part of the request URL.
     * @return Returns true if the audit device was successfully disabled.
     */
    public suspend fun disable(path: String): Boolean
}

/**
 * Implementation of [VaultSystemAudit].
 */
public class VaultSystemAuditImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultSystemAudit {

    public companion object {

        /**
         * Creates a new instance of [VaultSystemAuditImpl] using the provided HttpClient and optional parent path.
         * @param client HttpClient to interact with API.
         * @param parentPath The optional parent path used for building the final path used to interact with endpoints.
         * @param builder Builder to define the authentication service.
         * @return The instance of [VaultSystemAuditImpl] that was built.
         */
        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultSystemAuditImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "audit"
    }

    /**
     * Builder class to simplify the creation of [VaultSystemAuditImpl].
     */
    public class Builder : ServiceBuilder<VaultSystemAuditImpl>() {

        public override var path: String = Default.PATH

        override fun buildWithFullPath(client: HttpClient, fullPath: String): VaultSystemAuditImpl = VaultSystemAuditImpl(
            client = client,
            path = fullPath
        )
    }

    override suspend fun list(): Map<String, AuditingDeviceResponse> {
        val response = client.get {
            url {
                appendPathSegments(this@VaultSystemAuditImpl.path)
            }
        }

        return response.decodeBodyJsonDataFieldObject()
    }

    override suspend fun enable(path: String, payload: AuditingEnableDevicePayload): Boolean {
        val response = client.post {
            url {
                appendPathSegments(this@VaultSystemAuditImpl.path, path)
            }
            contentType(ContentType.Application.Json)
            setBody(payload)
        }
        return response.status.isSuccess()
    }

    override suspend fun disable(path: String): Boolean {
        val response = client.delete {
            url {
                appendPathSegments(this@VaultSystemAuditImpl.path, path)
            }
        }
        return response.status.isSuccess()
    }
}
