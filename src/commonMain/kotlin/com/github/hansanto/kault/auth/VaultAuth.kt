package com.github.hansanto.kault.auth

import com.github.hansanto.kault.ServiceBuilder
import com.github.hansanto.kault.ServiceBuilderConstructor
import com.github.hansanto.kault.auth.approle.VaultAuthAppRole
import com.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import io.ktor.client.HttpClient

public class VaultAuth(
    public var token: String? = null,
    public val appRole: VaultAuthAppRole
) {

    public companion object : ServiceBuilderConstructor<VaultAuth, Builder> {

        public override operator fun invoke(client: HttpClient, parentPath: String?, builder: Builder.() -> Unit): VaultAuth =
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
    public class Builder : ServiceBuilder<VaultAuth>() {

        public var token: String? = null

        public override var path: String = Default.PATH

        /**
         * Builder to define authentication appRole service.
         */
        private var appRoleBuilder: VaultAuthAppRoleImpl.Builder.() -> Unit = {}

        override fun buildWithFullPath(client: HttpClient, fullPath: String): VaultAuth {
            return VaultAuth(
                token = token,
                appRole = VaultAuthAppRoleImpl.Builder().apply(appRoleBuilder).build(client, fullPath)
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
