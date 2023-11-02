package com.github.hansanto.kault.auth.approle

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.auth.approle.payload.CreateOrUpdatePayload
import com.github.hansanto.kault.exception.VaultAPIException
import com.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlin.test.Test
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest

private const val DEFAULT_ROLE_NAME = "test"

class VaultAuthAppRoleTest {

    private lateinit var appRole: VaultAuthAppRole

    private fun test(testBody: suspend TestScope.() -> Unit): TestResult {
        return runTest {
            val client = VaultClient {
                url = "http://localhost:8200"
            }

            client.auth.token = "root"
            appRole = client.auth.appRole

            runCatching {
                appRole.list().collect {
                    appRole.delete(it) shouldBe true
                }
            }

            testBody()
        }
    }

    @Test
    fun createWithoutOptions() = test {
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldNotThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    @Test
    fun createWithOptions() = test {
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        val payload = readJson<CreateOrUpdatePayload>("cases/auth/approle/create.json")
        appRole.createOrUpdate(DEFAULT_ROLE_NAME, payload) shouldBe true
        shouldNotThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }
}


