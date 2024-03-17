package io.github.hansanto.kault.auth

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.approle.VaultAuthAppRole
import io.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import io.github.hansanto.kault.auth.common.response.LoginResponse
import io.github.hansanto.kault.auth.kubernetes.VaultAuthKubernetes
import io.github.hansanto.kault.auth.kubernetes.VaultAuthKubernetesImpl
import io.ktor.client.HttpClient
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Service to interact with Vault auth API.
 */
public class VaultAuth(
    /**
     * Token used to interact with API.
     */
    public var token: String? = null,

    /**
     * Authentication appRole service.
     */
    public val appRole: VaultAuthAppRole,

    /**
     * Authentication kubernetes service.
     */
    public val kubernetes: VaultAuthKubernetes
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

        /**
         * Builder to define authentication kubernetes service.
         */
        private var kubernetesBuilder: BuilderDsl<VaultAuthKubernetesImpl.Builder> = {}

        override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultAuth {
            return VaultAuth(
                token = token,
                appRole = VaultAuthAppRoleImpl.Builder().apply(appRoleBuilder).build(client, completePath),
                kubernetes = VaultAuthKubernetesImpl.Builder().apply(kubernetesBuilder).build(client, completePath)
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

        /**
         * Sets the authentication kubernetes service builder.
         *
         * @param builder Builder to create [VaultAuthKubernetesImpl] instance.
         */
        public fun kubernetes(builder: BuilderDsl<VaultAuthKubernetesImpl.Builder>) {
            kubernetesBuilder = builder
        }
    }

    /**
     * Process the login request using [VaultAuth]'s service and set the [token][VaultAuth.token] from the response.
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
