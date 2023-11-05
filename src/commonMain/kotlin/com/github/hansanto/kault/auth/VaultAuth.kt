package com.github.hansanto.kault.auth

import com.github.hansanto.kault.ServiceBuilder
import com.github.hansanto.kault.auth.approle.VaultAuthAppRole
import com.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import com.github.hansanto.kault.extension.addChildPath
import io.ktor.client.HttpClient

public class VaultAuth(
    client: HttpClient,
    path: String = Default.PATH,
    public var token: String? = null,
    appRoleBuilder: VaultAuthAppRoleImpl.Builder.() -> Unit = Default.appRoleBuilder
) {

    public companion object {

        /**
         * Create a new instance of [VaultAuth] using the builder pattern.
         * @param builder Builder to create the instance.
         * @return Instance of [VaultAuth].
         */
        public inline operator fun invoke(client: HttpClient, builder: Builder.() -> Unit): VaultAuth =
            Builder().apply(builder).build(client)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "auth"

        /**
         * Default authentication approle service builder.
         */
        public val appRoleBuilder: VaultAuthAppRoleImpl.Builder.() -> Unit = {}
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
        private var appRole: VaultAuthAppRoleImpl.Builder.() -> Unit = Default.appRoleBuilder

        override fun build(client: HttpClient, parentPath: String?): VaultAuth {
            return VaultAuth(
                client = client,
                path = parentPath?.addChildPath(path) ?: path,
                token = token
            )
        }

        /**
         * Sets the authentication appRole service builder.
         *
         * @param builder Builder to create [VaultAuthAppRoleImpl] instance.
         */
        public fun appRole(builder: VaultAuthAppRoleImpl.Builder.() -> Unit) {
            appRole = builder
        }
    }

    public val appRole: VaultAuthAppRole = VaultAuthAppRoleImpl(client, path, appRoleBuilder)
}
