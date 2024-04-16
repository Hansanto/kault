package io.github.hansanto.kault.auth

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import io.github.hansanto.kault.auth.approle.payload.AppRoleLoginPayload
import io.github.hansanto.kault.auth.common.common.TokenInfo
import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.auth.common.response.LoginResponse
import io.github.hansanto.kault.auth.kubernetes.VaultAuthKubernetesImpl
import io.github.hansanto.kault.auth.token.VaultAuthTokenImpl
import io.github.hansanto.kault.auth.userpass.VaultAuthUserpassImpl
import io.github.hansanto.kault.system.auth.enable
import io.github.hansanto.kault.util.DEFAULT_ROLE_NAME
import io.github.hansanto.kault.util.ROOT_TOKEN
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomLong
import io.github.hansanto.kault.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant

class VaultAuthTest : ShouldSpec({

    lateinit var client: VaultClient
    lateinit var auth: VaultAuth

    beforeSpec {
        client = createVaultClient()
        auth = client.auth

        runCatching {
            client.system.auth.enable("approle") {
                type = "approle"
            }
        }
    }

    afterSpec {
        client.close()
    }

    beforeTest {
        val appRole = auth.appRole
        runCatching {
            appRole.list().forEach {
                appRole.delete(it) shouldBe true
            }
        }
    }

    should("use default path if not set in builder") {
        VaultAuth.Default.PATH shouldBe "auth"

        val built = VaultAuth(client.client, null) {
        }

        built.tokenInfo shouldBe null
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
        }

        built.tokenInfo shouldBe defaultTokenInfo(randomToken)
        (built.appRole as VaultAuthAppRoleImpl).path shouldBe "$parentPath/$builderPath/$appRolePath"
        (built.kubernetes as VaultAuthKubernetesImpl).path shouldBe "$parentPath/$builderPath/$kubernetesPath"
        (built.userpass as VaultAuthUserpassImpl).path shouldBe "$parentPath/$builderPath/$userpassPath"
        (built.token as VaultAuthTokenImpl).path shouldBe "$parentPath/$builderPath/$tokenPath"
    }

    should("set token info with null value") {
        auth.tokenInfo = null
        assertLoginReplaceToken(auth)
    }

    should("set token info with non-null value") {
        auth.tokenInfo = TokenInfo(randomString())
        assertLoginReplaceToken(auth)
    }

    should("set token with null value") {
        auth.tokenInfo = TokenInfo(randomString())
        auth.setToken(null)
        auth.tokenInfo shouldBe null
    }

    should("set token with non-null value") {
        auth.tokenInfo = TokenInfo(
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

        val token = randomString()
        auth.setToken(token)
        auth.tokenInfo shouldBe defaultTokenInfo(token)
    }

    should("get token will return token if token info is not null") {
        val token = randomString()
        auth.tokenInfo = TokenInfo(token)
        auth.getToken() shouldBe token
    }

    should("get token will return null if token info is null") {
        auth.tokenInfo = null
        auth.getToken() shouldBe null
    }
})

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

    auth.tokenInfo?.token shouldBe loginResponse.clientToken
}

private suspend fun createLoginPayload(auth: VaultAuth): AppRoleLoginPayload {
    val appRole = auth.appRole

    val oldToken = auth.tokenInfo

    auth.tokenInfo = TokenInfo(ROOT_TOKEN) // to create role and generate secret id
    appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
    val secretId = appRole.generateSecretID(DEFAULT_ROLE_NAME).secretId
    val roleId = appRole.readRoleID(DEFAULT_ROLE_NAME).roleId

    auth.tokenInfo = oldToken

    return AppRoleLoginPayload(roleId, secretId)
}
