package com.github.hansanto.kault.auth.approle

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.exception.VaultAPIException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class VaultAuthAppRoleTest {

    @Test
    fun create() = runTest {
        val client = VaultClient {
            url = "http://localhost:8200"
        }

        client.auth.token = "root"
        val appRole = client.auth.appRole

        shouldThrow<VaultAPIException> { appRole.read("test") }

        appRole.createOrUpdate("test") shouldBe true
        val roleNameInfo = appRole.read("test")
    }
}
