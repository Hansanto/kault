package io.github.hansanto.kault.system.auth

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.system.auth.payload.AuthTuneConfigurationParametersPayload
import io.github.hansanto.kault.system.auth.payload.EnableMethodPayload
import io.github.hansanto.kault.system.auth.response.AuthReadConfigurationResponse
import io.github.hansanto.kault.system.auth.response.AuthReadTuningInformationResponse
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.replaceTemplateString
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

private const val DEFAULT_METHOD = "approle"

class VaultSystemAuthTest : ShouldSpec({

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

    should("use default path if not set in builder") {
        VaultSystemAuthImpl.Default.PATH shouldBe "auth"

        val built = VaultSystemAuthImpl(client.client, null) {
        }

        built.path shouldBe VaultSystemAuthImpl.Default.PATH
    }

    should("use custom values in the builder") {
        val randomPath = randomString()
        val parentPath = randomString()

        val built = VaultSystemAuthImpl(client.client, parentPath) {
            path = randomPath
        }

        built.path shouldBe "$parentPath/$randomPath"
    }

    should("list with no additional auth methods") {
        val response = auth.list()
        val expected = readJson<Map<String, AuthReadConfigurationResponse>>("cases/sys/auth/list/without_additional/expected.json")
        response shouldBe replaceTemplateString(response, expected)
    }

    should("list with additional auth methods") {
        val payload = readJson<EnableMethodPayload>("cases/sys/auth/list/with_additional/given.json")
        auth.enable(DEFAULT_METHOD, payload) shouldBe true

        val response = auth.list()
        val expected = readJson<Map<String, AuthReadConfigurationResponse>>("cases/sys/auth/list/with_additional/expected.json")
        response shouldBe replaceTemplateString(response, expected)
    }

    should("throw exception when enabling non-existing method with empty payload") {
        val exception = shouldThrow<Exception> {
            auth.enable(DEFAULT_METHOD) {}
        }

        (exception !is VaultAPIException) shouldBe true

        shouldThrow<VaultAPIException> {
            auth.readConfiguration(DEFAULT_METHOD)
        }
    }

    should("enable method & read configuration with minimal payload") {
        auth.enable(DEFAULT_METHOD) {
            type = DEFAULT_METHOD
        } shouldBe true

        val response = auth.readConfiguration(DEFAULT_METHOD)
        val expected = readJson<AuthReadConfigurationResponse>("cases/sys/auth/enable/without_options/expected.json")
        response shouldBe replaceTemplateString(response, expected)
    }

    should("enable method & read configuration with full payload") {
        val payload = readJson<EnableMethodPayload>("cases/sys/auth/enable/with_options/given.json")
        auth.enable(DEFAULT_METHOD, payload) shouldBe true

        val response = auth.readConfiguration(DEFAULT_METHOD)
        val expected = readJson<AuthReadConfigurationResponse>("cases/sys/auth/enable/with_options/expected.json")
        response shouldBe replaceTemplateString(response, expected)
    }

    should("throw exception when disabling non-existing method") {
        shouldThrow<VaultAPIException> {
            auth.disable("token")
        }
    }

    should("throw exception when reading configuration of non-existing method") {
        auth.disable(DEFAULT_METHOD) shouldBe true
        shouldThrow<VaultAPIException> {
            auth.readConfiguration(DEFAULT_METHOD)
        }
    }

    should("disable method if enabled") {
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

    should("throw exception when tuning non-existing method") {
        shouldThrow<VaultAPIException> {
            auth.tune(DEFAULT_METHOD)
        }
    }

    should("tune with default values") {
        assertTune(
            auth,
            null,
            "cases/sys/auth/tune/without_options/expected.json"
        )
    }

    should("tune with all defined values") {
        assertTune(
            auth,
            "cases/sys/auth/tune/with_options/given.json",
            "cases/sys/auth/tune/with_options/expected.json"
        )
    }

    should("tune using builder with default values") {
        assertTuneWithBuilder(
            auth,
            null,
            "cases/sys/auth/tune/without_options/expected.json"
        )
    }

    should("tune using builder with all defined values") {
        assertTuneWithBuilder(
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
    auth.enable(DEFAULT_METHOD) { type = DEFAULT_METHOD } shouldBe true

    val payload = givenPath?.let { readJson<AuthTuneConfigurationParametersPayload>(it) }
        ?: AuthTuneConfigurationParametersPayload()

    auth.tune(DEFAULT_METHOD, payload) shouldBe true
    auth.readTuning(DEFAULT_METHOD) shouldBe readJson<AuthReadTuningInformationResponse>(expectedPath)
}

private suspend fun assertTuneWithBuilder(
    auth: VaultSystemAuth,
    givenPath: String?,
    expectedPath: String
) {
    auth.enable(DEFAULT_METHOD) { type = DEFAULT_METHOD } shouldBe true

    val payload = givenPath?.let { readJson<AuthTuneConfigurationParametersPayload>(it) }
        ?: AuthTuneConfigurationParametersPayload()

    auth.tune(DEFAULT_METHOD) {
        auditNonHmacRequestKeys = payload.auditNonHmacRequestKeys
        auditNonHmacResponseKeys = payload.auditNonHmacResponseKeys
        allowedResponseHeaders = payload.allowedResponseHeaders
        passthroughRequestHeaders = payload.passthroughRequestHeaders
        defaultLeaseTTL = payload.defaultLeaseTTL
        description = payload.description
        listingVisibility = payload.listingVisibility
        maxLeaseTTL = payload.maxLeaseTTL
        pluginVersion = payload.pluginVersion
        options = payload.options
        tokenType = payload.tokenType
        userLockoutConfig = payload.userLockoutConfig
    } shouldBe true
    auth.readTuning(DEFAULT_METHOD) shouldBe readJson<AuthReadTuningInformationResponse>(expectedPath)
}
