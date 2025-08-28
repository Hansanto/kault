package io.github.hansanto.kault.system.auth

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.system.auth.payload.AuthTuneConfigurationParametersPayload
import io.github.hansanto.kault.system.auth.payload.EnableMethodPayload
import io.github.hansanto.kault.system.auth.response.AuthReadConfigurationResponse
import io.github.hansanto.kault.system.auth.response.AuthReadTuningInformationResponse
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.disableAllAuth
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.replaceTemplateString
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

private const val DEFAULT_METHOD = "approle"

class VaultSystemAuthTest {

    lateinit var client: VaultClient
    lateinit var auth: VaultSystemAuth

    @BeforeTest
    fun onBefore() = runTest {
        client = createVaultClient()
        auth = client.system.auth
        disableAllAuth(client)
    }

    @AfterTest
    fun onAfter() = runTest {
        disableAllAuth(client)
    }

    @Test
    fun `should use default path if not set in builder`() = runTest {
        VaultSystemAuthImpl.Default.PATH shouldBe "auth"

        val built = VaultSystemAuthImpl(client.client, null) {
        }

        built.path shouldBe VaultSystemAuthImpl.Default.PATH
    }

    @Test
    fun `should use custom values in the builder`() = runTest {
        val randomPath = randomString()
        val parentPath = randomString()

        val built = VaultSystemAuthImpl(client.client, parentPath) {
            path = randomPath
        }

        built.path shouldBe "$parentPath/$randomPath"
    }

    @Test
    fun `should list with no additional auth methods`() = runTest {
        val response = auth.list()
        val expected =
            readJson<Map<String, AuthReadConfigurationResponse>>(
                "cases/sys/auth/list/without_additional/expected.json"
            )
        response shouldBe replaceTemplateString(response, expected)
    }

    @Test
    fun `should list with additional auth methods`() = runTest {
        val payload = readJson<EnableMethodPayload>("cases/sys/auth/list/with_additional/given.json")
        auth.enable(DEFAULT_METHOD, payload) shouldBe true

        val response = auth.list()
        val expected =
            readJson<Map<String, AuthReadConfigurationResponse>>(
                "cases/sys/auth/list/with_additional/expected.json"
            )
        response shouldBe replaceTemplateString(response, expected)
    }

    @Test
    fun `should throw exception when enabling non-existing method with empty payload`() = runTest {
        val exception = shouldThrow<Exception> {
            auth.enable(DEFAULT_METHOD) {}
        }

        (exception !is VaultAPIException) shouldBe true

        shouldThrow<VaultAPIException> {
            auth.readConfiguration(DEFAULT_METHOD)
        }
    }

    @Test
    fun `should enable method and read configuration with minimal payload`() = runTest {
        auth.enable(DEFAULT_METHOD) {
            type = DEFAULT_METHOD
        } shouldBe true

        val response = auth.readConfiguration(DEFAULT_METHOD)
        val expected =
            readJson<AuthReadConfigurationResponse>("cases/sys/auth/enable/without_options/expected.json")
        response shouldBe replaceTemplateString(response, expected)
    }

    @Test
    fun `should enable method and read configuration with full payload`() = runTest {
        val payload = readJson<EnableMethodPayload>("cases/sys/auth/enable/with_options/given.json")
        auth.enable(DEFAULT_METHOD, payload) shouldBe true

        val response = auth.readConfiguration(DEFAULT_METHOD)
        val expected = readJson<AuthReadConfigurationResponse>("cases/sys/auth/enable/with_options/expected.json")
        response shouldBe replaceTemplateString(response, expected)
    }

    @Test
    fun `should throw exception when disabling non-existing method`() = runTest {
        shouldThrow<VaultAPIException> {
            auth.disable("token")
        }
    }

    @Test
    fun `should throw exception when reading configuration of non-existing method`() = runTest {
        auth.disable(DEFAULT_METHOD) shouldBe true
        shouldThrow<VaultAPIException> {
            auth.readConfiguration(DEFAULT_METHOD)
        }
    }

    @Test
    fun `should disable method if enabled`() = runTest {
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

    @Test
    fun `should throw exception when tuning non-existing method`() = runTest {
        shouldThrow<VaultAPIException> {
            auth.tune(DEFAULT_METHOD)
        }
    }

    @Test
    fun `should tune with default values`() = runTest {
        assertTune(
            auth,
            null,
            "cases/sys/auth/tune/without_options/expected.json"
        )
    }

    @Test
    fun `should tune with all defined values`() = runTest {
        assertTune(
            auth,
            "cases/sys/auth/tune/with_options/given.json",
            "cases/sys/auth/tune/with_options/expected.json"
        )
    }

    @Test
    fun `should tune using builder with default values`() = runTest {
        assertTuneWithBuilder(
            auth,
            null,
            "cases/sys/auth/tune/without_options/expected.json"
        )
    }

    @Test
    fun `should tune using builder with all defined values`() = runTest {
        assertTuneWithBuilder(
            auth,
            "cases/sys/auth/tune/with_options/given.json",
            "cases/sys/auth/tune/with_options/expected.json"
        )
    }
}

private suspend fun assertTune(auth: VaultSystemAuth, givenPath: String?, expectedPath: String) {
    auth.enable(DEFAULT_METHOD) { type = DEFAULT_METHOD } shouldBe true

    val payload = givenPath?.let { readJson<AuthTuneConfigurationParametersPayload>(it) }
        ?: AuthTuneConfigurationParametersPayload()

    auth.tune(DEFAULT_METHOD, payload) shouldBe true
    auth.readTuning(DEFAULT_METHOD) shouldBe readJson<AuthReadTuningInformationResponse>(expectedPath)
}

private suspend fun assertTuneWithBuilder(auth: VaultSystemAuth, givenPath: String?, expectedPath: String) {
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
