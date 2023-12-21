package io.github.hansanto.kault.auth

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.approle.payload.LoginPayload
import io.github.hansanto.kault.auth.approle.response.LoginResponse
import io.github.hansanto.kault.system.auth.enable
import io.github.hansanto.kault.util.ROOT_TOKEN
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private const val DEFAULT_ROLE_NAME = "test"

class VaultAuthTest : FunSpec({

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

    test("login should set token if null") {
        auth.token = null
        loginShouldReplaceToken(auth)
    }

    test("login should replace token if not null") {
        auth.token = randomString()
        loginShouldReplaceToken(auth)
    }

})

private suspend fun loginShouldReplaceToken(auth: VaultAuth) {
    val payload = createLoginPayload(auth)

    val loginResponse: LoginResponse
    auth.login {
        appRole.login(payload).apply {
            loginResponse = this
        }
    }

    auth.token shouldBe loginResponse.clientToken
}

private suspend fun createLoginPayload(auth: VaultAuth): LoginPayload {
    val appRole = auth.appRole

    val oldToken = auth.token

    auth.token = ROOT_TOKEN // to create role and generate secret id
    appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
    val secretId = appRole.generateSecretID(DEFAULT_ROLE_NAME).secretId
    val roleId = appRole.readRoleID(DEFAULT_ROLE_NAME).roleId

    auth.token = oldToken

    return LoginPayload(roleId, secretId)
}
