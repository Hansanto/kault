package io.github.hansanto.kault.auth

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import io.github.hansanto.kault.auth.approle.payload.AppRoleLoginPayload
import io.github.hansanto.kault.auth.common.common.TokenInfo
import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.auth.common.response.LoginResponse
import io.github.hansanto.kault.auth.kubernetes.VaultAuthKubernetesImpl
import io.github.hansanto.kault.auth.token.VaultAuthTokenImpl
import io.github.hansanto.kault.auth.token.createToken
import io.github.hansanto.kault.auth.token.response.TokenCreateResponse
import io.github.hansanto.kault.auth.token.response.toTokenInfo
import io.github.hansanto.kault.auth.userpass.VaultAuthUserpassImpl
import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.util.DEFAULT_ROLE_NAME
import io.github.hansanto.kault.util.ROOT_TOKEN
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.enableAuthMethod
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomLong
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.revokeAllAppRoleData
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.use
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class VaultAuthTest : ShouldSpec({

    lateinit var client: VaultClient
    lateinit var auth: VaultAuth

    beforeTest {
        client = createVaultClient()
        auth = client.auth

        enableAuthMethod(client, "approle")
        revokeAllAppRoleData(client)
    }

    afterTest {
        client.close()
    }

    should("throw exception if renew before duration is under or equal to 0") {
        fun checkThrowConstructor(duration: Duration) {
            shouldThrow<IllegalArgumentException> {
                VaultAuth(client.client, null) {
                    renewBeforeExpiration = duration
                }
            }
        }
        checkThrowConstructor(0.seconds)
        checkThrowConstructor((-1).milliseconds)
        checkThrowConstructor((-1).seconds)
        checkThrowConstructor((-1).days)
    }

    should("not throw exception if renew before duration is above 0") {
        fun checkNotThrowConstructor(duration: Duration) {
            shouldNotThrow<Exception> {
                VaultAuth(client.client, null) {
                    renewBeforeExpiration = duration
                }
            }
        }
        checkNotThrowConstructor(1.milliseconds)
        checkNotThrowConstructor(1.seconds)
        checkNotThrowConstructor(1.days)
    }

    should("use default values if not set in builder") {
        VaultAuth.Default.PATH shouldBe "auth"

        val built = VaultAuth(client.client, null) {
        }

        built.getTokenInfo() shouldBe null
        built.autoRenewToken shouldBe true
        built.renewBeforeExpiration shouldBe 10.minutes
        (built.appRole as VaultAuthAppRoleImpl).path shouldBe "${VaultAuth.Default.PATH}/${VaultAuthAppRoleImpl.Default.PATH}"
        (built.kubernetes as VaultAuthKubernetesImpl).path shouldBe "${VaultAuth.Default.PATH}/${VaultAuthKubernetesImpl.Default.PATH}"
        (built.userpass as VaultAuthUserpassImpl).path shouldBe "${VaultAuth.Default.PATH}/${VaultAuthUserpassImpl.Default.PATH}"
        (built.token as VaultAuthTokenImpl).path shouldBe "${VaultAuth.Default.PATH}/${VaultAuthTokenImpl.Default.PATH}"
    }

    should("use custom values in the builder") {
        val randomToken = randomString()

        val builderPath = randomString()
        val parentPath = randomString()

        val appRolePath = randomString()
        val kubernetesPath = randomString()
        val userpassPath = randomString()
        val tokenPath = randomString()
        val autoRenewToken = randomBoolean()
        val renewBeforeExpiration = randomLong(1L..1000L).seconds

        val built = VaultAuth(client.client, parentPath) {
            path = builderPath
            setToken(randomToken)
            appRole {
                path = appRolePath
            }
            kubernetes {
                path = kubernetesPath
            }
            userpass {
                path = userpassPath
            }
            token {
                path = tokenPath
            }
            this.autoRenewToken = autoRenewToken
            this.renewBeforeExpiration = renewBeforeExpiration
        }

        built.getTokenInfo() shouldBe defaultTokenInfo(randomToken)
        built.autoRenewToken shouldBe autoRenewToken
        built.renewBeforeExpiration shouldBe renewBeforeExpiration
        (built.appRole as VaultAuthAppRoleImpl).path shouldBe "$parentPath/$builderPath/$appRolePath"
        (built.kubernetes as VaultAuthKubernetesImpl).path shouldBe "$parentPath/$builderPath/$kubernetesPath"
        (built.userpass as VaultAuthUserpassImpl).path shouldBe "$parentPath/$builderPath/$userpassPath"
        (built.token as VaultAuthTokenImpl).path shouldBe "$parentPath/$builderPath/$tokenPath"

        val tokenInfo = TokenInfo(
            randomString(),
            randomString(),
            listOf(randomString()),
            mapOf(randomString() to randomString()),
            Instant.DISTANT_FUTURE,
            true,
            randomString(),
            TokenType.entries.random(),
            true,
            randomLong()
        )

        VaultAuth(client.client, null) {
            tokenInfo {
                this.token = tokenInfo.token
                this.accessor = tokenInfo.accessor
                this.tokenPolicies = tokenInfo.tokenPolicies
                this.metadata = tokenInfo.metadata
                this.expirationDate = tokenInfo.expirationDate
                this.renewable = tokenInfo.renewable
                this.entityId = tokenInfo.entityId
                this.tokenType = tokenInfo.tokenType
                this.orphan = tokenInfo.orphan
                this.numUses = tokenInfo.numUses
            }
        }.getTokenInfo() shouldBe tokenInfo

        VaultAuth(client.client, null) {
            tokenInfo(tokenInfo)
        }.getTokenInfo() shouldBe tokenInfo
    }

    should("start auto renew job when creating a new instance") {
        val tokenCreated = client.auth.token.createToken {
            renewable = true
            ttl = 1.days
        }
        val tokenCreatedInfo = tokenCreated.toTokenInfo()
        val tokenExpirationDate = tokenCreatedInfo.expirationDate!!

        createVaultClient {
            renewBeforeExpiration = 10.days
            autoRenewToken = true
            tokenInfo(tokenCreatedInfo)
        }.use {
            delay(100.milliseconds)
            val newTokenInfoAfterDelay = it.auth.getTokenInfo()!!
            val newExpirationDate = newTokenInfoAfterDelay.expirationDate!!

            (newExpirationDate > Clock.System.now()) shouldBe true
            (tokenExpirationDate < newExpirationDate) shouldBe true
            newTokenInfoAfterDelay shouldBe tokenCreatedInfo.copy(
                expirationDate = newExpirationDate
            )
        }
    }

    should("set token info with null value") {
        auth.setTokenInfo(null)
        assertLoginReplaceToken(auth)
    }

    should("set token info with non-null value") {
        auth.setTokenInfo(TokenInfo(randomString()))
        assertLoginReplaceToken(auth)
    }

    should("set token with null value") {
        auth.setTokenInfo(TokenInfo(randomString()))
        auth.setToken(null)
        auth.getTokenInfo() shouldBe null
    }

    should("set token with non-null value") {
        auth.setTokenInfo(
            TokenInfo(
                randomString(),
                randomString(),
                listOf(randomString()),
                mapOf(randomString() to randomString()),
                Instant.DISTANT_FUTURE,
                true,
                randomString(),
                TokenType.entries.random(),
                true,
                randomLong()
            )
        )

        val token = randomString()
        auth.setToken(token)
        auth.getTokenInfo() shouldBe defaultTokenInfo(token)
    }

    should("get token will return token if token info is not null") {
        val token = randomString()
        auth.setTokenInfo(TokenInfo(token))
        auth.getToken() shouldBe token
    }

    should("get token will return null if token info is null") {
        auth.setTokenInfo(null)
        auth.getToken() shouldBe null
    }

    should("enable auto-renew token will change the state") {
        auth.disableAutoRenewToken()
        auth.enableAutoRenewToken() shouldBe true
        auth.autoRenewToken shouldBe true
        auth.enableAutoRenewToken() shouldBe false
    }

    should("disable auto-renew token will change the state") {
        auth.enableAutoRenewToken()
        auth.disableAutoRenewToken() shouldBe true
        auth.autoRenewToken shouldBe false
        auth.disableAutoRenewToken() shouldBe false
    }

    should("renew token once if the renewal is enabled after setting new token") {
        createVaultClient {
            renewBeforeExpiration = 1.seconds
            autoRenewToken = false
        }.use {
            val tmpAuth = it.auth

            val tmpToken = createRenewToken(tmpAuth, 3.seconds)
            val tmpTokenInfo = tmpToken.toTokenInfo()
            val oldExpirationDate = tmpTokenInfo.expirationDate!!
            tmpAuth.setTokenInfo(tmpTokenInfo)

            tmpAuth.enableAutoRenewToken()
            delay(2.5.seconds)
            tmpAuth.disableAutoRenewToken()

            val authTokenInfoAfterDelay = tmpAuth.getTokenInfo()!!
            val newExpirationDate = authTokenInfoAfterDelay.expirationDate!!
            (newExpirationDate > oldExpirationDate + 1.seconds) &&
                (newExpirationDate <= oldExpirationDate + 1.2.seconds) &&
                (newExpirationDate > Clock.System.now()) shouldBe true

            authTokenInfoAfterDelay shouldBe tmpTokenInfo.copy(
                expirationDate = newExpirationDate
            )
        }
    }

    should("renew token once if the renewal is enabled before setting new token") {
        createVaultClient {
            renewBeforeExpiration = 1.seconds
            autoRenewToken = false
        }.use {
            val tmpAuth = it.auth
            tmpAuth.enableAutoRenewToken()

            val tmpToken = createRenewToken(tmpAuth, 3.seconds)
            val tmpTokenInfo = tmpToken.toTokenInfo()
            val oldExpirationDate = tmpTokenInfo.expirationDate!!
            tmpAuth.setTokenInfo(tmpTokenInfo)

            delay(2.5.seconds)
            tmpAuth.disableAutoRenewToken()

            val authTokenInfoAfterDelay = tmpAuth.getTokenInfo()!!
            val newExpirationDate = authTokenInfoAfterDelay.expirationDate!!
            (newExpirationDate > oldExpirationDate + 1.seconds) &&
                (newExpirationDate <= oldExpirationDate + 1.2.seconds) &&
                (newExpirationDate > Clock.System.now()) shouldBe true

            authTokenInfoAfterDelay shouldBe tmpTokenInfo.copy(
                expirationDate = newExpirationDate
            )
        }
    }

    should("renew token several times if the renewal is enabled") {
        createVaultClient {
            renewBeforeExpiration = 5.seconds
            autoRenewToken = false
        }.use {
            val tmpAuth = it.auth

            val tmpToken = createRenewToken(tmpAuth, 1.seconds)
            val tmpTokenInfo = tmpToken.toTokenInfo()
            val oldExpirationDate = tmpTokenInfo.expirationDate!!

            var previousExpirationDate = oldExpirationDate
            fun verifyNewTokenInfo() {
                val authTokenInfoAfterDelay = tmpAuth.getTokenInfo()!!
                val newExpirationDate = authTokenInfoAfterDelay.expirationDate!!
                (newExpirationDate > previousExpirationDate) && (newExpirationDate > Clock.System.now()) shouldBe true

                authTokenInfoAfterDelay shouldBe tmpTokenInfo.copy(
                    expirationDate = newExpirationDate
                )

                previousExpirationDate = newExpirationDate
            }

            tmpAuth.setTokenInfo(tmpTokenInfo)
            tmpAuth.enableAutoRenewToken()
            repeat(5) {
                delay(100.milliseconds)
                verifyNewTokenInfo()
            }
        }
    }

    should("not renew token if it's not renewable") {
        val tmpToken = createNonRenewToken(auth, 100.milliseconds)
        val tmpTokenInfo = tmpToken.toTokenInfo()
        auth.setTokenInfo(tmpTokenInfo)

        auth.enableAutoRenewToken()
        delay(1.seconds)
        auth.disableAutoRenewToken()

        auth.getTokenInfo() shouldBe tmpTokenInfo
    }

    should("not renew token if auto-renew is disabled before setting new token") {
        auth.disableAutoRenewToken()

        val tmpToken = createRenewToken(auth, 100.milliseconds)
        val tmpTokenInfo = tmpToken.toTokenInfo()
        auth.setTokenInfo(tmpTokenInfo)

        delay(1.seconds)

        val authTokenInfoAfterDelay = auth.getTokenInfo()!!
        authTokenInfoAfterDelay shouldBe tmpTokenInfo
        (authTokenInfoAfterDelay.expirationDate!! <= Clock.System.now()) shouldBe true
    }

    should("not renew token if auto-renew is disabled before expiration of the new token") {
        createVaultClient {
            renewBeforeExpiration = 1.seconds
            autoRenewToken = false
        }.use {
            val tmpAuth = it.auth
            tmpAuth.enableAutoRenewToken()

            val tmpToken = createRenewToken(tmpAuth, 2.seconds)
            val tmpTokenInfo = tmpToken.toTokenInfo()
            tmpAuth.setTokenInfo(tmpTokenInfo)

            tmpAuth.disableAutoRenewToken()
            delay(2.seconds)

            tmpAuth.getTokenInfo() shouldBe tmpTokenInfo
        }
    }
})

private suspend fun createRenewToken(auth: VaultAuth, leaseDuration: VaultDuration): TokenCreateResponse {
    val tokenService = auth.token
    return tokenService.createToken {
        this.renewable = true
        this.ttl = leaseDuration
    }
}

private suspend fun createNonRenewToken(auth: VaultAuth, leaseDuration: VaultDuration): TokenCreateResponse {
    val tokenService = auth.token
    return tokenService.createToken {
        this.renewable = false
        this.ttl = leaseDuration
    }
}

private fun defaultTokenInfo(token: String) = TokenInfo(
    token,
    null,
    emptyList(),
    emptyMap(),
    null,
    false,
    "",
    TokenType.SERVICE,
    true,
    0
)

private suspend fun assertLoginReplaceToken(auth: VaultAuth) {
    val payload = createLoginPayload(auth)

    val loginResponse: LoginResponse
    auth.login {
        appRole.login(payload).apply {
            loginResponse = this
        }
    }

    auth.getToken() shouldBe loginResponse.clientToken
}

private suspend fun createLoginPayload(auth: VaultAuth): AppRoleLoginPayload {
    val appRole = auth.appRole

    val oldToken = auth.getTokenInfo()

    auth.setToken(ROOT_TOKEN) // to create role and generate secret id
    appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
    val secretId = appRole.generateSecretID(DEFAULT_ROLE_NAME).secretId
    val roleId = appRole.readRoleID(DEFAULT_ROLE_NAME).roleId

    auth.setTokenInfo(oldToken)

    return AppRoleLoginPayload(roleId, secretId)
}
