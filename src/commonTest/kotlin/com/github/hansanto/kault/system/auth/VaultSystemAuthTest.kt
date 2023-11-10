package com.github.hansanto.kault.system.auth

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.exception.VaultAPIException
import com.github.hansanto.kault.system.auth.payload.AuthTuneConfigurationParametersPayload
import com.github.hansanto.kault.system.auth.payload.EnableMethodPayload
import com.github.hansanto.kault.system.auth.response.AuthReadConfigurationResponse
import com.github.hansanto.kault.system.auth.response.AuthReadTuningInformationResponse
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
        auth.list()
            .filterKeys { !it.contains("token") } // Cannot disable token auth
            .keys
            .forEach {
                auth.disable(it)
            }
        auth.list().size shouldBe 1
    }

    afterSpec {
        client.close()
    }

    test("list with no additional auth methods") {
        val response = auth.list()
        val expected =
            readJson<Map<String, AuthReadConfigurationResponse>>("cases/sys/auth/list/without_additional/expected.json")
        response shouldBe replaceDynamicFields(response, expected)
    }

    test("list with additional auth methods") {
        val payload = readJson<EnableMethodPayload>("cases/sys/auth/list/with_additional/given.json")
        auth.enable(DEFAULT_METHOD, payload) shouldBe true

        val response = auth.list()
        val expected =
            readJson<Map<String, AuthReadConfigurationResponse>>("cases/sys/auth/list/with_additional/expected.json")
        response shouldBe replaceDynamicFields(response, expected)
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
        response shouldBe replaceDynamicFields(response, expected)
    }

    test("enable method & read configuration with full payload") {
        val payload = readJson<EnableMethodPayload>("cases/sys/auth/enable/with_options/given.json")
        auth.enable(DEFAULT_METHOD, payload) shouldBe true

        val response = auth.readConfiguration(DEFAULT_METHOD)
        val expected = readJson<AuthReadConfigurationResponse>("cases/sys/auth/enable/with_options/expected.json")
        response shouldBe replaceDynamicFields(response, expected)
    }

    test("disable token method") {
        shouldThrow<VaultAPIException> {
            auth.disable("token")
        }
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

    test("tune method with non-existing method") {
        shouldThrow<VaultAPIException> {
            auth.tune(DEFAULT_METHOD)
        }
    }

    test("tune with empty payload") {
        assertTune(
            auth,
            null,
            "cases/sys/auth/tune/without_options/expected.json"
        )
    }

    test("tune with full payload") {
        assertTune(
            auth,
            "cases/sys/auth/tune/with_options/given.json",
            "cases/sys/auth/tune/with_options/expected.json"
        )
    }
})

private suspend fun assertTune(
    auth: VaultSystemAuth,
    givenPath: String?,
    expectedPath: String
) {
    auth.enable(DEFAULT_METHOD) {
        type = DEFAULT_METHOD
    } shouldBe true

    val payload = givenPath?.let { readJson<AuthTuneConfigurationParametersPayload>(it) } ?: AuthTuneConfigurationParametersPayload()
    auth.tune(DEFAULT_METHOD, payload) shouldBe true

    val tuneInfo = auth.readTuning(DEFAULT_METHOD)
    val expected = readJson<AuthReadTuningInformationResponse>(expectedPath)
    tuneInfo shouldBe expected
}

private fun replaceDynamicFields(
    response: Map<String, AuthReadConfigurationResponse>,
    expected: Map<String, AuthReadConfigurationResponse>
) = expected.mapValues {
    val responseConfig = response[it.key] ?: error("Missing key ${it.key} in response")
    replaceDynamicFields(responseConfig, it.value)
}

private fun replaceDynamicFields(
    response: AuthReadConfigurationResponse,
    expected: AuthReadConfigurationResponse
) = expected.copy(
    accessor = response.accessor,
    runningPluginVersion = response.runningPluginVersion,
    uuid = response.uuid
)
