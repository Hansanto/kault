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
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.toList

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

    test("list with no roles") {
        shouldThrow<VaultAPIException> {
            appRole.list().toList()
        }
    }

    test("list with roles") {
        val roles = List(10) { "test-$it" }
        roles.forEach { appRole.createOrUpdate(it) shouldBe true }
        appRole.list().toList() shouldBe roles
    }

    test("create without options and read") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldNotThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    test("create with options and read") {
        val given = readJson<CreateOrUpdatePayload>("cases/auth/approle/create/given.json")
        appRole.createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true

        val expected = readJson<AppRoleReadRoleResponse>("cases/auth/approle/create/expected.json")
        appRole.read(DEFAULT_ROLE_NAME) shouldBe expected
    }

    test("delete non-existing role") {
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        appRole.delete(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    test("delete existing role") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldNotThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        appRole.delete(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    test("read non-existing roleId") {
        shouldThrow<VaultAPIException> { appRole.readRoleID(DEFAULT_ROLE_NAME) }
    }

    test("read existing roleId") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val response = appRole.readRoleID(DEFAULT_ROLE_NAME)
        response.roleId.length shouldNotBe 0
    }
})
