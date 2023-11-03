package com.github.hansanto.kault.auth.approle

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.auth.approle.payload.CreateOrUpdatePayload
import com.github.hansanto.kault.auth.approle.response.AppRoleReadRoleResponse
import com.github.hansanto.kault.exception.VaultAPIException
import com.github.hansanto.kault.system.auth.enableMethod
import com.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private const val DEFAULT_ROLE_NAME = "test"

class VaultAuthAppRoleTest : FunSpec({

    lateinit var appRole: VaultAuthAppRole

    beforeSpec {
        val client = VaultClient {
            url = "http://localhost:8200"
        }

        client.auth.token = "root"
        appRole = client.auth.appRole

        runCatching {
            client.system.auth.enableMethod("approle") {
                type = "approle"
            } shouldBe true
        }
    }

    beforeTest {
        runCatching {
            appRole.list().collect {
                appRole.delete(it) shouldBe true
            }
        }

        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    xtest("create without options") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldNotThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    test("create with options") {
        val given = readJson<CreateOrUpdatePayload>("cases/auth/approle/create/given.json")
        appRole.createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true

        val expected = readJson<AppRoleReadRoleResponse>("cases/auth/approle/create/expected.json")
        appRole.read(DEFAULT_ROLE_NAME) shouldBe expected
    }
})
