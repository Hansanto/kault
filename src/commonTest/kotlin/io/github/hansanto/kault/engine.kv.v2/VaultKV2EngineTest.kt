package io.github.hansanto.kault.engine.kv.v2

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2ConfigureRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2WriteRequest
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadConfigurationResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadResponse
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.hours

class VaultKV2EngineTest : FunSpec({

    lateinit var client: VaultClient
    lateinit var kv2: VaultKV2Engine

    beforeSpec {
        client = createVaultClient()
        kv2 = client.secret.kv2
    }

    beforeTest {
        // Reset the configuration to have the same starting point for each test
        kv2.configure(
            KvV2ConfigureRequest(
                casRequired = false,
                deleteVersionAfter = 1.hours,
                maxVersions = 100
            )
        )
    }

    afterSpec {
        client.close()
    }

    test("configure without options") {
        val currentConfiguration = kv2.readConfiguration()
        kv2.configure() shouldBe true
        val response = kv2.readConfiguration()
        response shouldBe currentConfiguration
    }

    test("configure with options") {
        val given = readJson<KvV2ConfigureRequest>("cases/engine/kv/v2/configure/with_options/expected.json")
        kv2.configure(given) shouldBe true
        val response = kv2.readConfiguration()
        val expected =
            readJson<KvV2ReadConfigurationResponse>("cases/engine/kv/v2/configure/with_options/expected.json")
        response shouldBe expected
    }

    test("read secret when secret does not exist") {
        shouldThrow<VaultAPIException> {
            kv2.readSecret("test")
        }
    }

    test("read a previous secret version") {
        val path = randomString()

        val data = mapOf(randomString() to randomString())
        val version1WriteResponse = kv2.createOrUpdateSecret(path) {
            data(data)
        }

        kv2.createOrUpdateSecret(path) {
            data(mapOf(randomString() to randomString(), randomString() to randomString()))
        }

        val readResponse = kv2.readSecret(path, version1WriteResponse.version)
        val version1Data = readResponse.data<Map<String, String>>()
        version1Data shouldBe data
    }

    test("read the latest secret version") {
        val path = randomString()

        kv2.createOrUpdateSecret(path) {
            data(mapOf(randomString() to randomString()))
        }

        val updateData = mapOf(randomString() to randomString(), randomString() to randomString())
        val version2WriteResponse = kv2.createOrUpdateSecret(path) {
            data(updateData)
        }

        val readResponse = kv2.readSecret(path)
        readResponse.data<Map<String, String>>() shouldBe updateData

        val readTargetVersionResponse = kv2.readSecret(path, version2WriteResponse.version)
        readTargetVersionResponse.data<Map<String, String>>() shouldBe updateData
    }

    test("create new secret") {
        val path = randomString()
        val writeGiven = readJson<KvV2WriteRequest>("cases/engine/kv/v2/create_secret/create_given.json")
        val response = kv2.createOrUpdateSecret(path, writeGiven)
        val writeResponse = readJson<KvV2WriteRequest>("cases/engine/kv/v2/create_secret/create_expected.json")
        response shouldBe writeResponse

        val readResponse = kv2.readSecret(path)
        val expected = readJson<KvV2ReadResponse>("cases/engine/kv/v2/create_secret/read_expected.json")
        readResponse shouldBe expected
    }

    test("update existing secret") {
        val path = randomString()
        val writeGiven = readJson<KvV2WriteRequest>("cases/engine/kv/v2/update_secret/create_given.json")
        kv2.createOrUpdateSecret(path, writeGiven)
        val writeUpdateGiven = readJson<KvV2WriteRequest>("cases/engine/kv/v2/update_secret/update_given_2.json")
        val writeResponse = readJson<KvV2WriteRequest>("cases/engine/kv/v2/update_secret/update_expected.json")
        writeUpdateGiven shouldBe writeResponse

        val readResponse = kv2.readSecret(path)
        val expected = readJson<KvV2ReadResponse>("cases/engine/kv/v2/update_secret/read_expected.json")
        readResponse shouldBe expected
    }
})
