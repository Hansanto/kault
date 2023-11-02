package com.github.hansanto.kault.auth.approle

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.auth.approle.payload.CreateOrUpdatePayload
import com.github.hansanto.kault.exception.VaultAPIException
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
    }

    beforeTest {
        runCatching {
            appRole.list().collect {
                appRole.delete(it) shouldBe true
            }
        }
    }

    context("create or update") {
        test("create without options") {
            shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            shouldNotThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        }

        test("create with options") {
            shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
            val payload = readJson<CreateOrUpdatePayload>("cases/auth/approle/create.json")
            appRole.createOrUpdate(DEFAULT_ROLE_NAME, payload) shouldBe true
            shouldNotThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        }
    }
})
