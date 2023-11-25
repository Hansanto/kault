package io.github.hansanto.kault.engine.kv.v2

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2ConfigureRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2SubKeysRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2WriteRequest
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadConfigurationResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadSubkeysResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2WriteResponse
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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

        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())

        val readResponse = kv2.readSecret(path, version1WriteResponse.version)
        val version1Data = readResponse.data<Map<String, String>>()
        version1Data shouldBe data
    }

    test("read the latest secret version") {
        val path = randomString()

        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())

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
        val createGiven = readJson<KvV2WriteRequest>("cases/engine/kv/v2/create_secret/given.json")

        val writeResponse = kv2.createOrUpdateSecret(path, createGiven)
        val createExpectedResponse =
            readJson<KvV2WriteResponse>("cases/engine/kv/v2/create_secret/expected_write.json").copy(
                createdTime = writeResponse.createdTime,
                deletionTime = writeResponse.deletionTime
            )
        writeResponse shouldBe createExpectedResponse

        val readResponse = kv2.readSecret(path)
        val expected = readJson<KvV2ReadResponse>("cases/engine/kv/v2/create_secret/expected_read.json")
            .copy(
                metadata = readResponse.metadata.copy(
                    createdTime = writeResponse.createdTime
                )
            )
        readResponse shouldBe expected
    }

    test("update existing secret") {
        createAndUpdate(
            kv2,
            "cases/engine/kv/v2/update_secret/given_create.json",
            "cases/engine/kv/v2/update_secret/given_update.json",
            "cases/engine/kv/v2/update_secret/expected_update.json",
            "cases/engine/kv/v2/update_secret/expected_read.json",
            kv2::createOrUpdateSecret
        )
    }

    test("patch non-existing secret") {
        val path = randomString()
        shouldThrow<VaultAPIException> {
            kv2.patchSecret(path, simpleWriteRequestBuilder())
        }
    }

    test("patch with added secret part") {
        createAndUpdate(
            kv2,
            "cases/engine/kv/v2/patch_secret/given_create.json",
            "cases/engine/kv/v2/patch_secret/given_patch.json",
            "cases/engine/kv/v2/patch_secret/expected_patch.json",
            "cases/engine/kv/v2/patch_secret/expected_read.json",
            kv2::patchSecret
        )
    }

    test("read sub keys with non existing secret") {
        val path = randomString()
        shouldThrow<VaultAPIException> {
            kv2.readSecretSubKeys(path)
        }
    }

    test("read sub keys with non existing version") {
        val path = randomString()
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())

        shouldThrow<VaultAPIException> {
            kv2.readSecretSubKeys(path) {
                version = 2
            }
        }
    }

    test("read sub keys without options") {
        val path = randomString()

        val writeGiven = readJson<KvV2WriteRequest>("cases/engine/kv/v2/read_sub_keys/without_options/given.json")
        val writeResponse = kv2.createOrUpdateSecret(path, writeGiven)

        val readResponse = kv2.readSecretSubKeys(path)
        val expected =
            readJson<KvV2ReadSubkeysResponse>("cases/engine/kv/v2/read_sub_keys/without_options/expected.json").copy(
                metadata = readResponse.metadata.copy(
                    createdTime = writeResponse.createdTime
                )
            )
        readResponse shouldBe expected
    }

    test("read sub keys with options") {
        val path = randomString()

        val writeGiven = readJson<KvV2WriteRequest>("cases/engine/kv/v2/read_sub_keys/with_options/given_1.json")
        val writeResponse = kv2.createOrUpdateSecret(path, writeGiven)

        kv2.createOrUpdateSecret(
            path,
            readJson<KvV2WriteRequest>("cases/engine/kv/v2/read_sub_keys/with_options/given_2.json")
        )

        val parameters = readJson<KvV2SubKeysRequest>("cases/engine/kv/v2/read_sub_keys/with_options/parameters.json")
        val readResponse = kv2.readSecretSubKeys(path, parameters)
        val expected =
            readJson<KvV2ReadSubkeysResponse>("cases/engine/kv/v2/read_sub_keys/with_options/expected.json").copy(
                metadata = readResponse.metadata.copy(
                    createdTime = writeResponse.createdTime
                )
            )
        readResponse shouldBe expected
    }

    test("delete latest version without secret") {
        val path = randomString()
        kv2.deleteSecretLatestVersion(path) shouldBe true
    }

    test("delete latest version with secret and unique version") {
        val path = randomString()
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.deleteSecretLatestVersion(path)
        val readResponse = kv2.readSecret(path)
        readResponse.metadata.deletionTime shouldNotBe null
    }

    test("delete latest version with secret and multiple versions") {
        val path = randomString()
        val expected = mapOf("version" to "1")
        kv2.createOrUpdateSecret(path) {
            data(expected)
        }
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.deleteSecretLatestVersion(path)

        val readResponseLatestVersion = kv2.readSecret(path)
        readResponseLatestVersion.metadata.deletionTime shouldNotBe null

        val readResponse = kv2.readSecret(path, 1)
        readResponse.data<Map<String, String>>() shouldBe expected
    }

    test("delete secret with non existing secret") {
        val path = randomString()
        kv2.deleteSecretVersions(path, List(5) { it.toLong() }) shouldBe true
    }

    test("delete secret with non existing version") {
        val path = randomString()
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.deleteSecretVersions(path, listOf(10)) shouldBe true
    }

    test("delete secret with existing secret and version") {
        val path = randomString()

        val writeResponse1 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        val writeResponse2 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        val writeResponse3 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())

        kv2.deleteSecretVersions(path, listOf(writeResponse1.version, writeResponse3.version)) shouldBe true
        val readResponse1 = kv2.readSecret(path, writeResponse1.version)
        readResponse1.metadata.deletionTime shouldNotBe null

        kv2.readSecret(path, writeResponse2.version)
        val readResponse2 = kv2.readSecret(path, writeResponse3.version)
        readResponse2.metadata.deletionTime shouldNotBe null
    }
})

fun simpleWriteRequestBuilder(): BuilderDsl<KvV2WriteRequest.Builder> = {
    data(mapOf(randomString() to randomString()))
}

private suspend fun createAndUpdate(
    kv2: VaultKV2Engine,
    writeGiven: String,
    writeUpdateGiven: String,
    writeExpectedResponse: String,
    readExpectedResponse: String,
    update: suspend (String, KvV2WriteRequest) -> KvV2WriteResponse
) {
    val path = randomString()
    val writeGiven = readJson<KvV2WriteRequest>(writeGiven)
    kv2.createOrUpdateSecret(path, writeGiven)

    val patchGiven = readJson<KvV2WriteRequest>(writeUpdateGiven)
    val writeResponse = update(path, patchGiven)

    val writeExpectedResponse = readJson<KvV2WriteResponse>(writeExpectedResponse).copy(
        createdTime = writeResponse.createdTime,
        deletionTime = writeResponse.deletionTime
    )
    writeResponse shouldBe writeExpectedResponse

    val readResponse = kv2.readSecret(path)
    val expected = readJson<KvV2ReadResponse>(readExpectedResponse)
        .copy(
            metadata = readResponse.metadata.copy(
                createdTime = writeResponse.createdTime
            )
        )
    readResponse shouldBe expected
}
