package com.github.hansanto.kault.system.auth

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.exception.VaultAPIException
import com.github.hansanto.kault.system.auth.payload.EnableMethodPayload
import com.github.hansanto.kault.system.auth.response.AuthReadConfigurationResponse
import com.github.hansanto.kault.util.createVaultClient
import com.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

private const val DEFAULT_METHOD = "approle"

class VaultSystemAuthTest : FunSpec({

    lateinit var client: VaultClient
    lateinit var auth: VaultSystemAuth

    beforeSpec {
        client = createVaultClient()
        auth = client.system.auth
    }

    beforeTest {
        runCatching {
            auth.disable(DEFAULT_METHOD)
        }
    }

    afterSpec {
        client.close()
    }

    test("enable method & read configuration with empty payload") {
        val exception = shouldThrow<Exception> {
            auth.enable(DEFAULT_METHOD) {}
        }

        (exception !is VaultAPIException) shouldBe true

        shouldThrow<VaultAPIException> {
            auth.readConfiguration(DEFAULT_METHOD)
        }
    }

    test("enable method & read configuration with minimal payload") {
        auth.enable(DEFAULT_METHOD) {
            type = DEFAULT_METHOD
        } shouldBe true

        val response = auth.readConfiguration(DEFAULT_METHOD)
        val expected = readJson<AuthReadConfigurationResponse>("cases/sys/auth/enable/without_options/expected.json")
            .copy(
                accessor = response.accessor,
                runningPluginVersion = response.runningPluginVersion,
                uuid = response.uuid
            )
        response shouldBe expected
    }

    test("enable method & read configuration with full payload") {
        val payload = readJson<EnableMethodPayload>("cases/sys/auth/enable/with_options/given.json")

        auth.enable(DEFAULT_METHOD, payload) shouldBe true

        val response = auth.readConfiguration(DEFAULT_METHOD)
        val expected = readJson<AuthReadConfigurationResponse>("cases/sys/auth/enable/with_options/expected.json")
            .copy(
                accessor = response.accessor,
                runningPluginVersion = response.runningPluginVersion,
                uuid = response.uuid
            )
        response shouldBe expected
    }

    test("disable method with non-existing method") {
        auth.disable(DEFAULT_METHOD) shouldBe true
        shouldThrow<VaultAPIException> {
            auth.readConfiguration(DEFAULT_METHOD)
        }
    }

    test("disable method with existing method") {
        auth.enable(DEFAULT_METHOD) {
            type = DEFAULT_METHOD
        } shouldBe true

        shouldNotThrow<VaultAPIException> {
            auth.readConfiguration(DEFAULT_METHOD)
        }

        auth.disable(DEFAULT_METHOD) shouldBe true

        shouldThrow<VaultAPIException> {
            auth.readConfiguration(DEFAULT_METHOD)
        }
    }
})
