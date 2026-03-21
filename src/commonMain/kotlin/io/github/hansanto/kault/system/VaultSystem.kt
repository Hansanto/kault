package io.github.hansanto.kault.system

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.VaultAuth
import io.github.hansanto.kault.system.audit.VaultSystemAudit
import io.github.hansanto.kault.system.audit.VaultSystemAuditImpl
import io.github.hansanto.kault.system.auth.VaultSystemAuth
import io.github.hansanto.kault.system.auth.VaultSystemAuthImpl
import io.github.hansanto.kault.system.namespaces.VaultSystemNamespaces
import io.github.hansanto.kault.system.namespaces.VaultSystemNamespacesImpl
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
    public val audit: VaultSystemAudit,

    /**
     * Namespaces service.
     */
    public val namespaces: VaultSystemNamespaces,
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
            builder: BuilderDsl<Builder>
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
    public open class Builder : ServiceBuilder<VaultSystem>() {

        public override var path: String = Default.PATH

        /**
         * Builder to define authentication appRole service.
         */
        private var authBuilder: BuilderDsl<VaultSystemAuthImpl.Builder> = {}

        /**
         * Builder to define audit service.
         */
        private var auditBuilder: BuilderDsl<VaultSystemAuditImpl.Builder> = {}

        /**
         * Builder to define namespaces service.
         */
        private var namespacesBuilder: BuilderDsl<VaultSystemNamespacesImpl.Builder> = {}

        override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultSystem = VaultSystem(
            auth = VaultSystemAuthImpl.Builder().apply(authBuilder).build(client, completePath),
            audit = VaultSystemAuditImpl.Builder().apply(auditBuilder).build(client, completePath),
            namespaces = VaultSystemNamespacesImpl.Builder().apply(namespacesBuilder).build(client, completePath)
        )

        /**
         * Sets the authentication service builder.
         *
         * @param builder Builder to create [VaultSystemAuthImpl] instance.
         */
        public fun auth(builder: BuilderDsl<VaultSystemAuthImpl.Builder>) {
            authBuilder = builder
        }

        /**
         * Sets the audit service builder.
         *
         * @param builder Builder to create [VaultSystemAuditImpl] instance.
         */
        public fun audit(builder: BuilderDsl<VaultSystemAuditImpl.Builder>) {
            auditBuilder = builder
        }

        /**
         * Sets the namespaces service builder.
         *
         * @param builder Builder to create [VaultSystemNamespacesImpl] instance.
         */
        public fun namespaces(builder: BuilderDsl<VaultSystemNamespacesImpl.Builder>) {
            namespacesBuilder = builder
        }
    }
}
