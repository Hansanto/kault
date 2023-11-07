package com.github.hansanto.kault.auth

import com.github.hansanto.kault.ServiceBuilder
import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.auth.approle.VaultAuthAppRole
import com.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import io.ktor.client.HttpClient

/**
 * Service to interact with Vault auth API.
 */
public class VaultAuth(
    /**
     * Token used to interact with API.
     * This value is used through the header [VaultClient.Headers.token].
     */
    public var token: String? = null,

    /**
     * Authentication appRole service.
     */
    public val appRole: VaultAuthAppRole
) {

    public companion object {

        /**
         * Creates a new instance of [VaultAuth] using the provided HttpClient and optional parent path.
         * @param client HttpClient to interact with API.
         * @param parentPath The optional parent path used for building the final path used to interact with endpoints.
         * @param builder Builder to define the authentication service.
         * @return The instance of [VaultAuth] that was built.
         */
        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: Builder.() -> Unit
        ): VaultAuth = Builder().apply(builder).build(client, parentPath)
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
    public class Builder : ServiceBuilder<VaultAuth>() {

        /**
         * @see [VaultAuth.token]
         */
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
