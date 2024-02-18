package io.github.hansanto.kault.engine.kv.v2

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2ConfigureRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2SubKeysRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2WriteMetadataRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2WriteRequest
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadConfigurationResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadMetadataResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadSubkeysResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2WriteResponse
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
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

    test("builder default variables should be set correctly") {
        VaultKV2EngineImpl.Default.PATH shouldBe "secret"
    }

    test("builder should set values correctly") {
        val randomPath = randomString()
        val parentPath = randomString()

        val built = VaultKV2EngineImpl(client.client, parentPath) {
            path = randomPath
        }

        built.path shouldBe "$parentPath/$randomPath"
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

    test("create or update secret from builder") {
        val path = randomString()
        suspend fun createWithCas(data: Map<String, String>, cas: Int) = kv2.createOrUpdateSecret(path) {
            this.data(data)
            options {
                this.cas = cas
            }
        }

        suspend fun createAndRead(data: Map<String, String>, cas: Int) {
            createWithCas(data, cas)
            val readResponse = kv2.readSecret(path)
            val version1Data = readResponse.data<Map<String, String>>()
            version1Data shouldBe data
        }

        shouldThrow<VaultAPIException> {
            createWithCas(emptyMap(), 1)
        }

        createAndRead(mapOf(randomString() to randomString()), 0)
        createAndRead(mapOf(randomString() to randomString()), 1)
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
            readWriteResponse("cases/engine/kv/v2/create_secret/expected_write.json", writeResponse)
        writeResponse shouldBe createExpectedResponse

        val readResponse = kv2.readSecret(path)
        val expected = readSecretResponse("cases/engine/kv/v2/create_secret/expected_read.json", writeResponse)
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

    test("patch secret from builder") {
        createAndUpdate(
            kv2,
            "cases/engine/kv/v2/patch_secret/given_create.json",
            "cases/engine/kv/v2/patch_secret/given_patch.json",
            "cases/engine/kv/v2/patch_secret/expected_patch.json",
            "cases/engine/kv/v2/patch_secret/expected_read.json"
        ) { path, writeGiven ->
            kv2.patchSecret(path) {
                this.data(writeGiven.data)
                this.options {
                    this.cas = writeGiven.options?.cas
                }
            }
        }
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
            readSubKeysResponse("cases/engine/kv/v2/read_sub_keys/without_options/expected.json", writeResponse)
        readResponse shouldBe expected
    }

    test("read sub keys with options") {
        readSecretSubKeysWithOption(
            kv2,
            "cases/engine/kv/v2/read_sub_keys/with_options/given_1.json",
            "cases/engine/kv/v2/read_sub_keys/with_options/given_2.json",
            "cases/engine/kv/v2/read_sub_keys/with_options/parameters.json",
            "cases/engine/kv/v2/read_sub_keys/with_options/expected.json",
            kv2::readSecretSubKeys
        )
    }

    test("read secret sub keys from builder") {
        readSecretSubKeysWithOption(
            kv2,
            "cases/engine/kv/v2/read_sub_keys/with_options/given_1.json",
            "cases/engine/kv/v2/read_sub_keys/with_options/given_2.json",
            "cases/engine/kv/v2/read_sub_keys/with_options/parameters.json",
            "cases/engine/kv/v2/read_sub_keys/with_options/expected.json"
        ) { path, parameters ->
            kv2.readSecretSubKeys(path) {
                this.version = parameters.version
                this.depth = parameters.depth
            }
        }
    }

    test("delete latest version without secret") {
        val path = randomString()
        kv2.deleteSecretLatestVersion(path) shouldBe true
    }

    test("delete latest version with secret and unique version") {
        val path = randomString()
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.deleteSecretLatestVersion(path)
        kv2.readSecret(path).isDeleted() shouldBe true
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
        readResponseLatestVersion.isDeleted() shouldBe true

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
        kv2.readSecret(path).isDeleted() shouldBe false
    }

    test("delete secret with existing secret and version") {
        val path = randomString()

        val writeResponse1 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        val writeResponse2 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        val writeResponse3 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())

        kv2.deleteSecretVersions(path, listOf(writeResponse1.version, writeResponse3.version)) shouldBe true
        val readResponse1 = kv2.readSecret(path, writeResponse1.version)
        readResponse1.isDeleted() shouldBe true

        kv2.readSecret(path, writeResponse2.version)
        val readResponse2 = kv2.readSecret(path, writeResponse3.version)
        readResponse2.isDeleted() shouldBe true
    }

    test("undelete secret with non existing secret") {
        val path = randomString()
        kv2.undeleteSecretVersions(path, listOf(1)) shouldBe true
    }

    test("undelete secret with non existing version") {
        val path = randomString()
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.undeleteSecretVersions(path, listOf(10)) shouldBe true
        kv2.readSecret(path).isDeleted() shouldBe false
    }

    test("undelete secret with existing secret and version") {
        val path = randomString()
        val writeResponse1 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        val writeResponse2 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        val writeResponse3 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())

        kv2.deleteSecretVersions(
            path,
            listOf(writeResponse1.version, writeResponse2.version, writeResponse3.version)
        ) shouldBe true
        kv2.undeleteSecretVersions(path, listOf(writeResponse1.version, writeResponse3.version)) shouldBe true

        kv2.readSecret(path, writeResponse1.version).isDeleted() shouldBe false
        kv2.readSecret(path, writeResponse2.version).isDeleted() shouldBe true
        kv2.readSecret(path, writeResponse3.version).isDeleted() shouldBe false
    }

    test("destroy secret with non existing secret") {
        val path = randomString()
        kv2.destroySecretVersions(path, listOf(1)) shouldBe true
    }

    test("destroy secret with non existing version") {
        val path = randomString()
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.destroySecretVersions(path, listOf(10)) shouldBe true
        kv2.readSecret(path).isDestroyed() shouldBe false
    }

    test("destroy secret with existing secret and version") {
        val path = randomString()
        val writeResponse1 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        val writeResponse2 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())

        kv2.destroySecretVersions(path, listOf(writeResponse1.version)) shouldBe true

        kv2.readSecret(path, writeResponse1.version).isDestroyed() shouldBe true
        kv2.readSecret(path, writeResponse2.version).isDestroyed() shouldBe false

        kv2.undeleteSecretVersions(path, listOf(writeResponse1.version)) shouldBe true
        kv2.readSecret(path, writeResponse1.version).isDestroyed() shouldBe true
    }

    test("list with non-existing folder") {
        val path = randomString()
        shouldThrow<VaultAPIException> {
            kv2.listSecrets(path)
        }
    }

    test("list with existing folder") {
        val path = randomString()
        val key1 = randomString()
        val key2 = randomString()

        kv2.createOrUpdateSecret("$path/$key1", simpleWriteRequestBuilder())
        kv2.createOrUpdateSecret("$path/$key2/test", simpleWriteRequestBuilder())

        val response = kv2.listSecrets(path)
        response shouldContainExactlyInAnyOrder listOf(key1, "$key2/")
    }

    test("read secret metadata with non existing secret") {
        val path = randomString()
        shouldThrow<VaultAPIException> {
            kv2.readSecretMetadata(path)
        }
    }

    test("read secret metadata with existing secret") {
        val path = randomString()

        val writeResponse1 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        val writeResponse2 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.destroySecretVersions(path, listOf(writeResponse1.version))
        val response = kv2.readSecretMetadata(path)
        val expected = readJson<KvV2ReadMetadataResponse>("cases/engine/kv/v2/read_secret_metadata/expected.json").let {
            it.copy(
                createdTime = writeResponse1.createdTime,
                updatedTime = writeResponse2.createdTime,
                versions = it.versions.mapValues { (id, version) ->
                    val writeResponse = when (id) {
                        writeResponse1.version -> writeResponse1
                        writeResponse2.version -> writeResponse2
                        else -> throw IllegalStateException("Unexpected version id: $id")
                    }

                    version.copy(
                        createdTime = writeResponse.createdTime,
                        deletionTime = writeResponse.deletionTime
                    )
                }
            )
        }
        response shouldBe expected
    }

    test("create or update metadata with non existing secret") {
        val path = randomString()
        val writeGiven = readJson<KvV2WriteMetadataRequest>("cases/engine/kv/v2/update_metadata/without_secret/given.json")
        kv2.createOrUpdateMetadata(path, writeGiven) shouldBe true

        val readResponse = kv2.readSecretMetadata(path)
        val expected = readJson<KvV2ReadMetadataResponse>("cases/engine/kv/v2/update_metadata/without_secret/expected.json").copy(
            createdTime = readResponse.createdTime,
            updatedTime = readResponse.updatedTime
        )
        readResponse shouldBe expected
    }

    test("create or update metadata with existing secret") {
        updateMetadataWithSecret(
            kv2,
            "cases/engine/kv/v2/update_metadata/with_secret/given.json",
            "cases/engine/kv/v2/update_metadata/with_secret/expected.json",
            kv2::createOrUpdateMetadata
        )
    }

    test("create or update metadata from builder") {
        updateMetadataWithSecret(
            kv2,
            "cases/engine/kv/v2/update_metadata/with_secret/given.json",
            "cases/engine/kv/v2/update_metadata/with_secret/expected.json"
        ) { path, writeGiven ->
            kv2.createOrUpdateMetadata(path) {
                this.casRequired = writeGiven.casRequired
                this.maxVersions = writeGiven.maxVersions
                this.deleteVersionAfter = writeGiven.deleteVersionAfter
                this.customMetadata = writeGiven.customMetadata
            }
        }
    }

    test("patch metadata with non existing secret") {
        val path = randomString()
        val writeGiven = readJson<KvV2WriteMetadataRequest>("cases/engine/kv/v2/update_metadata/without_secret/given.json")
        kv2.patchMetadata(path, writeGiven) shouldBe false

        shouldThrow<VaultAPIException> {
            kv2.readSecretMetadata(path)
        }
    }

    test("patch metadata with existing secret") {
        updateMetadataWithSecret(
            kv2,
            "cases/engine/kv/v2/update_metadata/with_secret/given.json",
            "cases/engine/kv/v2/update_metadata/with_secret/expected.json",
            kv2::patchMetadata
        )
    }

    test("patch metadata from builder") {
        updateMetadataWithSecret(
            kv2,
            "cases/engine/kv/v2/update_metadata/with_secret/given.json",
            "cases/engine/kv/v2/update_metadata/with_secret/expected.json"
        ) { path, writeGiven ->
            kv2.patchMetadata(path) {
                this.casRequired = writeGiven.casRequired
                this.maxVersions = writeGiven.maxVersions
                this.deleteVersionAfter = writeGiven.deleteVersionAfter
                this.customMetadata = writeGiven.customMetadata
            }
        }
    }

    test("delete metadata and all versions with non existing secret") {
        val path = randomString()
        kv2.deleteMetadataAndAllVersions(path) shouldBe true
    }

    test("delete metadata and all versions with existing secret") {
        val path = randomString()
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.deleteMetadataAndAllVersions(path) shouldBe true
        shouldThrow<VaultAPIException> {
            kv2.readSecret(path)
        }
        shouldThrow<VaultAPIException> {
            kv2.readSecretMetadata(path)
        }
    }
})

private suspend fun updateMetadataWithSecret(
    kv2: VaultKV2Engine,
    writeGivenPath: String,
    readExpectedPath: String,
    updateMetadata: suspend (String, KvV2WriteMetadataRequest) -> Boolean
) {
    val path = randomString()
    val writeSecretResponse = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())

    val writeGiven = readJson<KvV2WriteMetadataRequest>(writeGivenPath)
    updateMetadata(path, writeGiven) shouldBe true

    val readResponse = kv2.readSecretMetadata(path)
    val expected = readJson<KvV2ReadMetadataResponse>(readExpectedPath).copy(
        createdTime = readResponse.createdTime,
        updatedTime = readResponse.createdTime,
        versions = readResponse.versions.mapValues { (_, version) ->
            version.copy(
                createdTime = writeSecretResponse.createdTime,
                deletionTime = writeSecretResponse.deletionTime
            )
        }
    )

    readResponse shouldBe expected
}

private suspend fun readSecretSubKeysWithOption(
    kv2: VaultKV2Engine,
    writeGiven1Path: String,
    writeGiven2Path: String,
    readPayloadPath: String,
    readExpectedPath: String,
    callReadSubKeys: suspend (String, KvV2SubKeysRequest) -> KvV2ReadSubkeysResponse
) {
    val path = randomString()

    val writeGiven = readJson<KvV2WriteRequest>(writeGiven1Path)
    val writeResponse = kv2.createOrUpdateSecret(path, writeGiven)

    kv2.createOrUpdateSecret(
        path,
        readJson<KvV2WriteRequest>(writeGiven2Path)
    )

    val parameters = readJson<KvV2SubKeysRequest>(readPayloadPath)
    val readResponse = callReadSubKeys(path, parameters)
    val expected = readSubKeysResponse(readExpectedPath, writeResponse)
    readResponse shouldBe expected
}

private suspend fun createAndUpdate(
    kv2: VaultKV2Engine,
    writeGivenPath: String,
    writeUpdateGivenPath: String,
    writeExpectedResponsePath: String,
    readExpectedResponsePath: String,
    update: suspend (String, KvV2WriteRequest) -> KvV2WriteResponse
) {
    val path = randomString()
    val writeGiven = readJson<KvV2WriteRequest>(writeGivenPath)
    kv2.createOrUpdateSecret(path, writeGiven)

    val patchGiven = readJson<KvV2WriteRequest>(writeUpdateGivenPath)
    val writeResponse = update(path, patchGiven)

    val writeExpectedResponse = readWriteResponse(writeExpectedResponsePath, writeResponse)
    writeResponse shouldBe writeExpectedResponse

    val readResponse = kv2.readSecret(path)
    val expected = readSecretResponse(readExpectedResponsePath, writeResponse)
    readResponse shouldBe expected
}

private fun readSubKeysResponse(
    path: String,
    writeResponse: KvV2WriteResponse
) = readJson<KvV2ReadSubkeysResponse>(path).let {
    it.copy(
        metadata = it.metadata.copy(
            createdTime = writeResponse.createdTime,
            deletionTime = writeResponse.deletionTime
        )
    )
}

private fun readSecretResponse(
    path: String,
    writeResponse: KvV2WriteResponse
) = readJson<KvV2ReadResponse>(path).let {
    it.copy(
        metadata = it.metadata.copy(
            createdTime = writeResponse.createdTime,
            deletionTime = writeResponse.deletionTime
        )
    )
}

private fun readWriteResponse(
    path: String,
    writeResponse: KvV2WriteResponse
) = readJson<KvV2WriteResponse>(path).copy(
    createdTime = writeResponse.createdTime,
    deletionTime = writeResponse.deletionTime
)

fun simpleWriteRequestBuilder(): BuilderDsl<KvV2WriteRequest.Builder> = {
    data(
        buildMap {
            val numberOfValues = (1..10).random()
            repeat(numberOfValues) {
                put(randomString(), randomString())
            }
        }
    )
}
