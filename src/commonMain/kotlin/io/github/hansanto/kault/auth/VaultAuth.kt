package io.github.hansanto.kault.auth

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.approle.VaultAuthAppRole
import io.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import io.github.hansanto.kault.auth.approle.response.LoginResponse
import io.ktor.client.HttpClient
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

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
            builder: BuilderDsl<Builder>
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
        private var appRoleBuilder: BuilderDsl<VaultAuthAppRoleImpl.Builder> = {}

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
        public fun appRole(builder: BuilderDsl<VaultAuthAppRoleImpl.Builder>) {
            appRoleBuilder = builder
        }
    }

    /**
     * Authenticates using the provided credentials and stores the token.
     *
     * Example of usage:
     * ```kotlin
     * login { appRole.login(payload) }
     * ```
     * @param loginRequest Function to authenticate and return the response.
     */
    public inline fun login(loginRequest: @KaultDsl VaultAuth.() -> LoginResponse) {
        contract {
            callsInPlace(loginRequest, InvocationKind.EXACTLY_ONCE)
        }
        token = loginRequest(this).clientToken
    }
}
