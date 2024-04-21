package io.github.hansanto.kault.auth

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.approle.VaultAuthAppRole
import io.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import io.github.hansanto.kault.auth.common.common.TokenInfo
import io.github.hansanto.kault.auth.common.response.LoginResponse
import io.github.hansanto.kault.auth.common.response.toTokenInfo
import io.github.hansanto.kault.auth.kubernetes.VaultAuthKubernetes
import io.github.hansanto.kault.auth.kubernetes.VaultAuthKubernetesImpl
import io.github.hansanto.kault.auth.token.VaultAuthToken
import io.github.hansanto.kault.auth.token.VaultAuthTokenImpl
import io.github.hansanto.kault.auth.token.renewToken
import io.github.hansanto.kault.auth.token.response.toTokenInfo
import io.github.hansanto.kault.auth.userpass.VaultAuthUserpass
import io.github.hansanto.kault.auth.userpass.VaultAuthUserpassImpl
import io.ktor.client.HttpClient
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Service to interact with Vault auth API.
 */
public class VaultAuth(
    /**
     * Contains information about the token used to interact with the API.
     */
    private var tokenInfo: TokenInfo?,

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
    public val token: VaultAuthToken,

    /**
     * Coroutine scope to renew the token when it is about to expire.
     * Should be a child of the http client to be canceled when the client is canceled.
     */
    private val renewCoroutineScope: CoroutineScope,

    /**
     * [VaultAuth.autoRenewToken]
     */
    autoRenewToken: Boolean = Default.AUTO_RENEW_TOKEN,

    /**
     * Duration before the token expiration to renew it.
     */
    public val renewBeforeExpiration: Duration = Default.RENEW_BEFORE_EXPIRATION
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

        /**
         * Default duration before the token expiration to renew it.
         */
        public val RENEW_BEFORE_EXPIRATION: Duration = 10.minutes

        /**
         * Default flag to check if the auto-renewal token feature is enabled.
         */
        public const val AUTO_RENEW_TOKEN: Boolean = false
    }

    /**
     * Builder class to simplify the creation of [VaultAuth].
     */
    public class Builder : ServiceBuilder<VaultAuth>() {

        public override var path: String = Default.PATH

        /**
         * [VaultAuth.renewBeforeExpiration]
         */
        public var renewBeforeExpiration: Duration = Default.RENEW_BEFORE_EXPIRATION

        /**
         * [VaultAuth.autoRenewToken]
         */
        public var autoRenewToken: Boolean = Default.AUTO_RENEW_TOKEN

        /**
         * [VaultAuth.tokenInfo]
         */
        public var tokenInfo: TokenInfo? = null

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
                renewCoroutineScope = CoroutineScope(SupervisorJob(client.coroutineContext.job) + Dispatchers.Default),
                autoRenewToken = autoRenewToken,
                renewBeforeExpiration = renewBeforeExpiration,
                tokenInfo = tokenInfo,
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
            tokenInfo(TokenInfo.Builder().apply(builder).build())
        }

        /**
         * Sets the token information.
         *
         * @param tokenInfo Token information to use for the next requests.
         */
        public fun tokenInfo(tokenInfo: TokenInfo) {
            this.tokenInfo = tokenInfo
        }

        /**
         * Set the [tokenInfo] builder from the provided token.
         * @param token Token to use for the next requests.
         */
        public fun setToken(token: String) {
            tokenInfo(TokenInfo(token))
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
     * Flag to check if the auto-renewal token feature is enabled.
     */
    public var autoRenewToken: Boolean = autoRenewToken
        private set

    /**
     * Job to renew the token when it is about to expire.
     */
    private var renewTokenJob: Job? = null

    init {
        require(renewBeforeExpiration > Duration.ZERO) { "The renew before expiration must be greater than 0" }

        if (autoRenewToken) {
            renewTokenJob = createRenewTokenJob()
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
        setTokenInfo(loginResponse.toTokenInfo())
    }

    /**
     * Set the [tokenInfo] from the provided [tokenInfo].
     * If the auto-renewal token feature is enabled, the job to renew the token will be restarted.
     * @param tokenInfo Token information to use for the next requests.
     */
    public fun setTokenInfo(tokenInfo: TokenInfo?) {
        this.tokenInfo = tokenInfo
        restartRenewTokenJob()
    }

    /**
     * Get the [tokenInfo] used to interact with the API.
     * @return The token information used to interact with the API.
     */
    public fun getTokenInfo(): TokenInfo? {
        return tokenInfo
    }

    /**
     * Set the [tokenInfo] from the provided token.
     * If the token is null, the [tokenInfo] will be null.
     * Otherwise, the [tokenInfo] will be a new instance of [TokenInfo] with the provided token only.
     * @param token Token to use for the next requests.
     */
    public fun setToken(token: String?) {
        setTokenInfo(token?.let { TokenInfo(it) })
    }

    /**
     * Get the token from the [tokenInfo].
     * If the [tokenInfo] is null, the token will be null.
     * @return The token used to interact with the API.
     */
    public fun getToken(): String? {
        return getTokenInfo()?.token
    }

    /**
     * Enable the auto-renewal token feature.
     * If it is already enabled, nothing will happen.
     * Otherwise, a job will be started to renew the token when it is about to expire.
     * @return True if the feature was enabled, if it was already enabled, false.
     */
    public fun enableAutoRenewToken(): Boolean {
        if (autoRenewToken) return false
        autoRenewToken = true
        renewTokenJob = createRenewTokenJob()
        return true
    }

    /**
     * Disable the auto-renewal token feature.
     * If it is already disabled, nothing will happen.
     * Otherwise, the job to renew the token will be canceled.
     * @return True if the feature was disabled, if it was already disabled, false.
     */
    public fun disableAutoRenewToken(): Boolean {
        if (!autoRenewToken) return false
        autoRenewToken = false
        renewTokenJob?.cancel("Auto-renew token feature is disabled")
        return true
    }

    /**
     * If the [autoRenewToken] is enabled, restart the job to renew the token.
     */
    private fun restartRenewTokenJob() {
        if (autoRenewToken) {
            renewTokenJob?.cancel("Token has changed")
            renewTokenJob = createRenewTokenJob()
        }
    }

    /**
     * Start a job to renew the token when it is about to expire.
     * @return The job that will renew the token.
     */
    private fun createRenewTokenJob(): Job = renewCoroutineScope.launch {
        while (this.isActive) {
            val tokenInformation = getTokenInfo()
            if (tokenInformation == null) {
                cancel("Token is undefined")
                return@launch
            }

            if (!tokenInformation.renewable) {
                cancel("Token is not renewable")
                return@launch
            }

            val expirationDate = tokenInformation.expirationDate
            if (expirationDate == null) {
                cancel("Token expiration date is undefined")
                return@launch
            }

            val now = Clock.System.now()
            if (now >= expirationDate) {
                cancel("Token is already expired")
                return@launch
            }

            val durationBeforeNow = expirationDate - now
            val timeToSleep = durationBeforeNow - renewBeforeExpiration
            delay(timeToSleep)

            try {
                renewToken(tokenInformation.token)
            } catch (e: Exception) {
                // If the token cannot be renewed, the job is canceled
                cancel("Token cannot be renewed", e)
            }
        }
    }

    /**
     * Renew the token using the [token] service.
     * The operation is thread-safe and will be executed only once at a time.
     * @param token Token to renew.
     */
    private suspend fun renewToken(token: String) {
        val renewResponse = this.token.renewToken {
            this.token = token
        }
        // By setting the value directly to the property, the renew job is not restarted
        tokenInfo = renewResponse.toTokenInfo()
    }
}
