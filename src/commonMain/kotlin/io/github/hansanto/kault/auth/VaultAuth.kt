package io.github.hansanto.kault.auth

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.approle.VaultAuthAppRole
import io.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import io.github.hansanto.kault.auth.common.common.TokenInfo
import io.github.hansanto.kault.auth.common.response.LoginResponse
import io.github.hansanto.kault.auth.kubernetes.VaultAuthKubernetes
import io.github.hansanto.kault.auth.kubernetes.VaultAuthKubernetesImpl
import io.github.hansanto.kault.auth.token.VaultAuthToken
import io.github.hansanto.kault.auth.token.VaultAuthTokenImpl
import io.github.hansanto.kault.auth.userpass.VaultAuthUserpass
import io.github.hansanto.kault.auth.userpass.VaultAuthUserpassImpl
import io.ktor.client.HttpClient
import kotlinx.datetime.Clock
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Service to interact with Vault auth API.
 */
public class VaultAuth(
    /**
     * Contains information about the token used to interact with the API.
     */
    public var tokenInfo: TokenInfo? = null,

    /**
     * Authentication appRole service.
     */
    public val appRole: VaultAuthAppRole,

    /**
     * Authentication username & password service.
     */
    public val userpass: VaultAuthUserpass,

    /**
     * Authentication kubernetes service.
     */
    public val kubernetes: VaultAuthKubernetes,

    /**
     * Authentication token service.
     */
    public val token: VaultAuthToken
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

        public override var path: String = Default.PATH

        /**
         * Builder to define token information.
         */
        public var tokenInfoBuilder: BuilderDsl<TokenInfo.Builder>? = null

        /**
         * Builder to define authentication appRole service.
         */
        private var appRoleBuilder: BuilderDsl<VaultAuthAppRoleImpl.Builder> = {}

        /**
         * Builder to define authentication username & password service.
         */
        private var userpassBuilder: BuilderDsl<VaultAuthUserpassImpl.Builder> = {}

        /**
         * Builder to define authentication kubernetes service.
         */
        private var kubernetesBuilder: BuilderDsl<VaultAuthKubernetesImpl.Builder> = {}

        /**
         * Builder to define authentication token service.
         */
        private var tokenBuilder: BuilderDsl<VaultAuthTokenImpl.Builder> = {}

        override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultAuth {
            return VaultAuth(
                tokenInfo = tokenInfoBuilder?.let { TokenInfo.Builder().apply(it).build() },
                appRole = VaultAuthAppRoleImpl.Builder().apply(appRoleBuilder).build(client, completePath),
                userpass = VaultAuthUserpassImpl.Builder().apply(userpassBuilder).build(client, completePath),
                kubernetes = VaultAuthKubernetesImpl.Builder().apply(kubernetesBuilder).build(client, completePath),
                token = VaultAuthTokenImpl.Builder().apply(tokenBuilder).build(client, completePath)
            )
        }

        /**
         * Sets the token information builder.
         *
         * @param builder Builder to create [TokenInfo] instance.
         */
        public fun tokenInfo(builder: BuilderDsl<TokenInfo.Builder>) {
            tokenInfoBuilder = builder
        }

        /**
         * Sets the token information using only the token.
         *
         * @param token The token to use.
         */
        public fun tokenInfo(token: String) {
            tokenInfoBuilder = {
                this.token = token
            }
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
         * Sets the authentication username & password service builder.
         *
         * @param builder Builder to create [VaultAuthUserpassImpl] instance.
         */
        public fun userpass(builder: BuilderDsl<VaultAuthUserpassImpl.Builder>) {
            userpassBuilder = builder
        }

        /**
         * Sets the authentication kubernetes service builder.
         *
         * @param builder Builder to create [VaultAuthKubernetesImpl] instance.
         */
        public fun kubernetes(builder: BuilderDsl<VaultAuthKubernetesImpl.Builder>) {
            kubernetesBuilder = builder
        }

        /**
         * Sets the authentication token service builder.
         *
         * @param builder Builder to create [VaultAuthTokenImpl] instance.
         */
        public fun token(builder: BuilderDsl<VaultAuthTokenImpl.Builder>) {
            tokenBuilder = builder
        }
    }

    /**
     * Process the login request using [VaultAuth]'s service and set the [token][VaultAuth.tokenInfo] from the response.
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
        val loginResponse = loginRequest(this)
        tokenInfo = TokenInfo(
            token = loginResponse.clientToken,
            accessor = loginResponse.accessor,
            tokenPolicies = loginResponse.tokenPolicies,
            metadata = loginResponse.metadata,
            expirationDate = Clock.System.now().plus(loginResponse.leaseDuration),
            renewable = loginResponse.renewable,
            entityId = loginResponse.entityId,
            tokenType = loginResponse.tokenType,
            orphan = loginResponse.orphan,
            numUses = loginResponse.numUses
        )
    }
}
