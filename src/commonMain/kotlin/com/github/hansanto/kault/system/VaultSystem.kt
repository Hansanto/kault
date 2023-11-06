package com.github.hansanto.kault.system

import com.github.hansanto.kault.ServiceBuilder
import com.github.hansanto.kault.ServiceBuilderConstructor
import com.github.hansanto.kault.auth.VaultAuth
import com.github.hansanto.kault.system.auth.VaultSystemAuth
import com.github.hansanto.kault.system.auth.VaultSystemAuthImpl
import io.ktor.client.HttpClient

public class VaultSystem(
    public val auth: VaultSystemAuth
) {

    public companion object : ServiceBuilderConstructor<VaultSystem, Builder> {

        public override operator fun invoke(client: HttpClient, parentPath: String?, builder: Builder.() -> Unit): VaultSystem =
            Builder().apply(builder).build(client, parentPath)
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
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder : ServiceBuilder<VaultSystem>() {

        public var token: String? = null

        public override var path: String = Default.PATH

        /**
         * Builder to define authentication appRole service.
         */
        private var authBuilder: VaultSystemAuthImpl.Builder.() -> Unit = {}

        override fun buildWithFullPath(client: HttpClient, fullPath: String): VaultSystem {
            return VaultSystem(
                auth = VaultSystemAuthImpl.Builder().apply(authBuilder).build(client, fullPath)
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
    }
}
