package com.github.hansanto.kault.auth

import com.github.hansanto.kault.ServiceBuilder
import com.github.hansanto.kault.auth.approle.VaultAuthAppRole
import com.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import com.github.hansanto.kault.extension.addURLChildPath
import io.ktor.client.HttpClient

public class VaultAuth(
    public var token: String? = null,
    public val appRole: VaultAuthAppRole
) {

    public companion object {

        /**
         * Create a new instance of [VaultAuth] using the builder pattern.
         * @param builder Builder to create the instance.
         * @return Instance of [VaultAuth].
         */
        public inline operator fun invoke(client: HttpClient, parentPath: String?, builder: Builder.() -> Unit): VaultAuth =
            Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "auth"
    }

    /**
     * Builder class to simplify the creation of [VaultAuth].
     */
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder : ServiceBuilder<VaultAuth> {

        public var token: String? = null

        public override var path: String = Default.PATH

        /**
         * Builder to define authentication appRole service.
         */
        private var appRoleBuilder: VaultAuthAppRoleImpl.Builder.() -> Unit = {}

        override fun build(client: HttpClient, parentPath: String?): VaultAuth {
            val entirePath = parentPath?.addURLChildPath(path) ?: path
            val appRole = VaultAuthAppRoleImpl.Builder().apply(appRoleBuilder).build(client, entirePath)
            return VaultAuth(
                token = token,
                appRole = appRole
            )
        }

        /**
         * Sets the authentication appRole service builder.
         *
         * @param builder Builder to create [VaultAuthAppRoleImpl] instance.
         */
        public fun appRole(builder: VaultAuthAppRoleImpl.Builder.() -> Unit) {
            appRoleBuilder = builder
        }
    }
}
