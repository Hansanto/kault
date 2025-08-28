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
import io.github.hansanto.kault.util.matcher.shouldBeBetween
import io.github.hansanto.kault.util.randomLong
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.revokeAllAppRoleData
import io.github.hansanto.kault.util.revokeAllTokenData
import io.github.hansanto.kault.util.runBlockingTest
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.comparables.shouldBeLessThan
import io.kotest.matchers.comparables.shouldBeLessThanOrEqualTo
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.time.Clock
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.Instant

class VaultAuthTest {

    lateinit var client: VaultClient
    lateinit var auth: VaultAuth

    @BeforeTest
    fun onBefore() = runTest {
        client = createVaultClient()
        auth = client.auth
        enableAuthMethod(client, "approle")
    }

    @AfterTest
    fun onAfter() = runTest {
        revokeAllAppRoleData(client)
        revokeAllTokenData(client)
        client.close()
    }

    @Test
    fun `should throw exception if renew before duration is under or equal to 0`() = runTest {
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

    @Test
    fun `should not throw exception if renew before duration is above 0`() = runTest {
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

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `should use default values if not set in builder`() = runTest {
        VaultAuth.Default.PATH shouldBe "auth"

        val built = VaultAuth(client.client, null) {
        }

        built.getTokenInfo() shouldBe null
        built.autoRenewToken shouldBe false
        built.renewBeforeExpiration shouldBe 10.minutes
        (built.appRole as VaultAuthAppRoleImpl).path shouldBe
            "${VaultAuth.Default.PATH}/${VaultAuthAppRoleImpl.Default.PATH}"
        (built.kubernetes as VaultAuthKubernetesImpl).path shouldBe
            "${VaultAuth.Default.PATH}/${VaultAuthKubernetesImpl.Default.PATH}"
        (built.userpass as VaultAuthUserpassImpl).path shouldBe
            "${VaultAuth.Default.PATH}/${VaultAuthUserpassImpl.Default.PATH}"
        (built.token as VaultAuthTokenImpl).path shouldBe
            "${VaultAuth.Default.PATH}/${VaultAuthTokenImpl.Default.PATH}"
    }

    @Test
    fun `should use custom values in the builder`() = runTest {
        val randomToken = randomString()

        val builderPath = randomString()
        val parentPath = randomString()

        val appRolePath = randomString()
        val kubernetesPath = randomString()
        val userpassPath = randomString()
        val tokenPath = randomString()
        val renewBeforeExpiration = randomLong(1L..1000L).seconds

        val built = VaultAuth(client.client, parentPath) {
            path = builderPath
            setTokenString(randomToken)
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
            this.renewBeforeExpiration = renewBeforeExpiration
        }

        built.getTokenInfo() shouldBe defaultTokenInfo(randomToken)
        built.autoRenewToken shouldBe false
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

    @Test
    fun `should do nothing when refresh token info with null token`() = runTest {
        auth.setTokenInfo(null)
        auth.refreshTokenInfo() shouldBe false
        auth.getTokenInfo() shouldBe null
    }

    @Test
    fun `should do nothing when refresh token info is equals to the current token`() = runTest {
        val newToken = auth.token.createToken()
        val tokenInfo = newToken.toTokenInfo()
        auth.setTokenInfo(tokenInfo)
        auth.refreshTokenInfo() shouldBe false
        auth.getTokenInfo() shouldBe tokenInfo
    }

    @Test
    fun `should refresh token info if only the token string is set`() = runTest {
        val newToken = auth.token.createToken()
        auth.setTokenString(newToken.clientToken)
        auth.refreshTokenInfo() shouldBe true
        auth.getTokenInfo() shouldBe newToken.toTokenInfo()
    }

    @Test
    fun `should start auto renew job when creating a new instance`() = runBlockingTest {
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
            it.auth.disableAutoRenewToken()

            val newTokenInfoAfterDelay = it.auth.getTokenInfo()!!
            val newExpirationDate = newTokenInfoAfterDelay.expirationDate!!

            newExpirationDate shouldBeGreaterThan Clock.System.now()
            tokenExpirationDate shouldBeLessThan newExpirationDate
            newTokenInfoAfterDelay shouldBe tokenCreatedInfo.copy(
                expirationDate = newExpirationDate
            )
        }
    }

    @Test
    fun `should set token info with null value`() = runTest {
        auth.setTokenInfo(null)
        assertLoginReplaceToken(auth)
    }

    @Test
    fun `should set token info with non-null value`() = runTest {
        auth.setTokenInfo(TokenInfo(randomString()))
        assertLoginReplaceToken(auth)
    }

    @Test
    fun `should set token with null value`() = runTest {
        auth.setTokenInfo(TokenInfo(randomString()))
        auth.setTokenString(null)
        auth.getTokenInfo() shouldBe null
    }

    @Test
    fun `should set token with non-null value`() = runTest {
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
        auth.setTokenString(token)
        auth.getTokenInfo() shouldBe defaultTokenInfo(token)
    }

    @Test
    fun `should get token will return token if token info is not null`() = runTest {
        val token = randomString()
        auth.setTokenInfo(TokenInfo(token))
        auth.getTokenString() shouldBe token
    }

    @Test
    fun `should get token will return null if token info is null`() = runTest {
        auth.setTokenInfo(null)
        auth.getTokenString() shouldBe null
    }

    @Test
    fun `should enable auto-renew token will change the state`() = runTest {
        auth.disableAutoRenewToken()
        auth.enableAutoRenewToken() shouldBe true
        auth.autoRenewToken shouldBe true
        auth.enableAutoRenewToken() shouldBe false
    }

    @Test
    fun `should disable auto-renew token will change the state`() = runTest {
        auth.enableAutoRenewToken()
        auth.disableAutoRenewToken() shouldBe true
        auth.autoRenewToken shouldBe false
        auth.disableAutoRenewToken() shouldBe false
    }

    @Test
    fun `should renew token once if the renewal is enabled after setting new token`() = runBlockingTest {
        createVaultClient {
            renewBeforeExpiration = 1.seconds
            autoRenewToken = false
        }.use {
            val tmpAuth = it.auth

            val tmpToken = createRenewToken(tmpAuth, 2.seconds)
            val tmpTokenInfo = tmpToken.toTokenInfo()
            val oldExpirationDate = tmpTokenInfo.expirationDate!!
            tmpAuth.setTokenInfo(tmpTokenInfo)

            tmpAuth.enableAutoRenewToken()
            delay(1.2.seconds)
            tmpAuth.disableAutoRenewToken()

            val authTokenInfoAfterDelay = tmpAuth.getTokenInfo()!!
            val newExpirationDate = authTokenInfoAfterDelay.expirationDate!!
            newExpirationDate shouldBeBetween (oldExpirationDate + 1.seconds)..(oldExpirationDate + 1.2.seconds)
            newExpirationDate shouldBeGreaterThan Clock.System.now()

            authTokenInfoAfterDelay shouldBe tmpTokenInfo.copy(
                expirationDate = newExpirationDate
            )
        }
    }

    @Test
    fun `should renew token once if the renewal is enabled before setting new token`() = runBlockingTest {
        createVaultClient {
            renewBeforeExpiration = 1.seconds
            autoRenewToken = false
        }.use {
            val tmpAuth = it.auth
            tmpAuth.enableAutoRenewToken()

            val tmpToken = createRenewToken(tmpAuth, 2.seconds)
            val tmpTokenInfo = tmpToken.toTokenInfo()
            val oldExpirationDate = tmpTokenInfo.expirationDate!!
            tmpAuth.setTokenInfo(tmpTokenInfo)

            delay(1.2.seconds)
            tmpAuth.disableAutoRenewToken()

            val authTokenInfoAfterDelay = tmpAuth.getTokenInfo()!!
            val newExpirationDate = authTokenInfoAfterDelay.expirationDate!!
            newExpirationDate shouldBeBetween (oldExpirationDate + 1.seconds)..(oldExpirationDate + 1.2.seconds)
            newExpirationDate shouldBeGreaterThan Clock.System.now()

            authTokenInfoAfterDelay shouldBe tmpTokenInfo.copy(
                expirationDate = newExpirationDate
            )
        }
    }

    @Test
    fun `should renew token several times if the renewal is enabled`() = runBlockingTest {
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
                newExpirationDate shouldBeGreaterThan previousExpirationDate
                newExpirationDate shouldBeGreaterThan Clock.System.now()

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

    @Test
    fun `should not renew token if it's not renewable`() = runTest {
        val tmpToken = createNonRenewToken(auth, 100.milliseconds)
        val tmpTokenInfo = tmpToken.toTokenInfo()
        auth.setTokenInfo(tmpTokenInfo)

        auth.enableAutoRenewToken()
        delay(1.seconds)
        auth.disableAutoRenewToken()

        auth.getTokenInfo() shouldBe tmpTokenInfo
    }

    @Test
    fun `should not renew token if auto-renew is disabled before setting new token`() = runBlockingTest {
        auth.disableAutoRenewToken()
        val leaseDuration = 1.seconds

        val tmpToken = createRenewToken(auth, leaseDuration)
        tmpToken.leaseDuration shouldBe leaseDuration
        val tmpTokenInfo = tmpToken.toTokenInfo()
        auth.setTokenInfo(tmpTokenInfo)

        delay(leaseDuration + 200.milliseconds)

        val authTokenInfoAfterDelay = auth.getTokenInfo()!!
        authTokenInfoAfterDelay shouldBe tmpTokenInfo
        authTokenInfoAfterDelay.expirationDate!! shouldBeLessThanOrEqualTo Clock.System.now()
    }

    @Test
    fun `should not renew token if auto-renew is disabled before expiration of the new token`() = runTest {
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
}

private suspend fun createRenewToken(auth: VaultAuth, leaseDuration: VaultDuration): TokenCreateResponse {
    val tokenService = auth.token
    return tokenService.createToken {
        this.renewable = true
        this.ttl = leaseDuration
        noParent = true
        period = leaseDuration
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

    auth.getTokenString() shouldBe loginResponse.clientToken
}

private suspend fun createLoginPayload(auth: VaultAuth): AppRoleLoginPayload {
    val appRole = auth.appRole

    val oldToken = auth.getTokenInfo()

    auth.setTokenString(ROOT_TOKEN) // to create role and generate secret id
    appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
    val secretId = appRole.generateSecretID(DEFAULT_ROLE_NAME).secretId
    val roleId = appRole.readRoleID(DEFAULT_ROLE_NAME).roleId

    auth.setTokenInfo(oldToken)

    return AppRoleLoginPayload(roleId, secretId)
}
