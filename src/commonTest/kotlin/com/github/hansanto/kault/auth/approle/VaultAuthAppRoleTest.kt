package com.github.hansanto.kault.auth.approle

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.auth.approle.payload.CreateOrUpdatePayload
import com.github.hansanto.kault.exception.VaultAPIException
import com.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test

private const val DEFAULT_ROLE_NAME = "test"

class VaultAuthAppRoleTest {

    private lateinit var appRole: VaultAuthAppRole

    @BeforeTest
    fun onBefore() {
        val client = VaultClient {
            url = "http://localhost:8200"
        }

        client.auth.token = "root"
        appRole = client.auth.appRole

        runCatching {
            val deletedAppRole = appRole.list().toList()
            println("Following role will be deleted: $deletedAppRole")
            deletedAppRole.forEach {
                println("deleting $it")
                appRole.delete(it) shouldBe true
                println("deleted $it")
            }
        }
    }

    @Test
    fun createWithoutOptions() = runTest {
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldNotThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    @Test
    fun createWithOptions() = runTest {
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        val payload = readJson<CreateOrUpdatePayload>("cases/auth/approle/create.json")
        appRole.createOrUpdate(DEFAULT_ROLE_NAME, payload) shouldBe true
        shouldNotThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }
}
