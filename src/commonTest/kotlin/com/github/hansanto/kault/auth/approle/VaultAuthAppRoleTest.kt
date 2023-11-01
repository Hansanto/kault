package com.github.hansanto.kault.auth.approle

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.exception.VaultException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class VaultAuthAppRoleTest {

    @Test
    fun create() = runTest {
        val client = VaultClient("http://localhost:8200")
        client.auth.token = "root"
        val appRole = client.auth.appRole

        shouldThrow<VaultException> { appRole.read("test") }

        appRole.createOrUpdate("test") shouldBe true
        val roleNameInfo = appRole.read("test")
    }
}
