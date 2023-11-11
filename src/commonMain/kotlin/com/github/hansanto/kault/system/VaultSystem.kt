package com.github.hansanto.kault.system

import com.github.hansanto.kault.ServiceBuilder
import com.github.hansanto.kault.auth.VaultAuth
import com.github.hansanto.kault.system.audit.VaultSystemAudit
import com.github.hansanto.kault.system.audit.VaultSystemAuditImpl
import com.github.hansanto.kault.system.auth.VaultSystemAuth
import com.github.hansanto.kault.system.auth.VaultSystemAuthImpl
import io.ktor.client.HttpClient

/**
 * Service to interact with Vault system API.
 */
public class VaultSystem(
    /**
     * Authentication service.
     */
    public val auth: VaultSystemAuth,

    /**
     * Audit service.
     */
    public val audit: VaultSystemAudit
) {

    public companion object {

        /**
         * Creates a new instance of [VaultSystem] using the provided HttpClient and optional parent path.
         * @param client HttpClient to interact with API.
         * @param parentPath The optional parent path used for building the final path used to interact with endpoints.
         * @param builder Builder to define the authentication service.
         * @return The instance of [VaultSystem] that was built.
         */
        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: Builder.() -> Unit
        ): VaultSystem = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "sys"
    }

    /**
     * Builder class to simplify the creation of [VaultAuth].
     */
    public class Builder : ServiceBuilder<VaultSystem>() {

        /**
         * @see [VaultAuth.token]
         */
        public var token: String? = null

        public override var path: String = Default.PATH

        /**
         * Builder to define authentication appRole service.
         */
        private var authBuilder: VaultSystemAuthImpl.Builder.() -> Unit = {}

        /**
         * Builder to define audit service.
         */
        private var auditBuilder: VaultSystemAuditImpl.Builder.() -> Unit = {}

        override fun buildWithFullPath(client: HttpClient, fullPath: String): VaultSystem {
            return VaultSystem(
                auth = VaultSystemAuthImpl.Builder().apply(authBuilder).build(client, fullPath),
                audit = VaultSystemAuditImpl.Builder().apply(auditBuilder).build(client, fullPath)
            )
        }

        /**
         * Sets the authentication service builder.
         *
         * @param builder Builder to create [VaultSystemAuthImpl] instance.
         */
        public fun auth(builder: VaultSystemAuthImpl.Builder.() -> Unit) {
            authBuilder = builder
        }

        /**
         * Sets the audit service builder.
         *
         * @param builder Builder to create [VaultSystemAuditImpl] instance.
         */
        public fun audit(builder: VaultSystemAuditImpl.Builder.() -> Unit) {
            auditBuilder = builder
        }
    }
}
