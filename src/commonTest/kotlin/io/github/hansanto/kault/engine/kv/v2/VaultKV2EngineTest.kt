package io.github.hansanto.kault.engine.kv.v2

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.VaultClient
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
import io.github.hansanto.kault.util.replaceTemplateString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.use

class VaultKV2EngineTest : ShouldSpec({

    lateinit var client: VaultClient
    lateinit var kv2: VaultKV2Engine
    lateinit var initialKv2Configuration: KvV2ReadConfigurationResponse

    beforeSpec {
        initialKv2Configuration = createVaultClient().use {
            it.secret.kv2.readConfiguration()
        }
    }

    beforeTest {
        client = createVaultClient()
        kv2 = client.secret.kv2
        // Reset the configuration to have the same starting point for each test
        kv2.configure(
            KvV2ConfigureRequest(
                casRequired = initialKv2Configuration.casRequired,
                deleteVersionAfter = initialKv2Configuration.deleteVersionAfter,
                maxVersions = initialKv2Configuration.maxVersions
            )
        )
    }

    afterTest {
        client.close()
    }

    should("use default path if not set in builder") {
        VaultKV2EngineImpl.Default.PATH shouldBe "secret"

        val built = VaultKV2EngineImpl(client.client, null) {
        }

        built.path shouldBe VaultKV2EngineImpl.Default.PATH
    }

    should("use custom path if set in builder") {
        val randomPath = randomString()
        val parentPath = randomString()

        val built = VaultKV2EngineImpl(client.client, parentPath) {
            path = randomPath
        }

        built.path shouldBe "$parentPath/$randomPath"
    }

    should("set configuration with default values") {
        val currentConfiguration = kv2.readConfiguration()
        kv2.configure() shouldBe true
        val response = kv2.readConfiguration()
        response shouldBe currentConfiguration
    }

    should("set configuration with all defined values") {
        val given = readJson<KvV2ConfigureRequest>("cases/engine/kv/v2/configure/with_options/expected.json")
        kv2.configure(given) shouldBe true
        val response = kv2.readConfiguration()
        val expected =
            readJson<KvV2ReadConfigurationResponse>("cases/engine/kv/v2/configure/with_options/expected.json")
        response shouldBe expected
    }

    should("create or update secret using builder") {
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

    should("throw exception when reading non-existing secret") {
        shouldThrow<VaultAPIException> {
            kv2.readSecret("test")
        }
    }

    should("read a previous secret version") {
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

    should("read the latest secret version") {
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

    should("create new secret") {
        val path = randomString()
        val createGiven = readJson<KvV2WriteRequest>("cases/engine/kv/v2/create_secret/given.json")

        val writeResponse = kv2.createOrUpdateSecret(path, createGiven)
        val createExpectedResponse = readJson<KvV2WriteResponse>("cases/engine/kv/v2/create_secret/expected_write.json")
        writeResponse shouldBe replaceTemplateString(createExpectedResponse, writeResponse)

        val readResponse = kv2.readSecret(path)
        val expected = readSecretResponse("cases/engine/kv/v2/create_secret/expected_read.json", writeResponse)
        readResponse shouldBe expected
    }

    should("update existing secret") {
        createAndUpdate(
            kv2,
            "cases/engine/kv/v2/update_secret/given_create.json",
            "cases/engine/kv/v2/update_secret/given_update.json",
            "cases/engine/kv/v2/update_secret/expected_update.json",
            "cases/engine/kv/v2/update_secret/expected_read.json",
            kv2::createOrUpdateSecret
        )
    }

    should("throw exception when patching non-existing secret") {
        val path = randomString()
        shouldThrow<VaultAPIException> {
            kv2.patchSecret(path, simpleWriteRequestBuilder())
        }
    }

    should("patch with added secret part") {
        createAndUpdate(
            kv2,
            "cases/engine/kv/v2/patch_secret/given_create.json",
            "cases/engine/kv/v2/patch_secret/given_patch.json",
            "cases/engine/kv/v2/patch_secret/expected_patch.json",
            "cases/engine/kv/v2/patch_secret/expected_read.json",
            kv2::patchSecret
        )
    }

    should("patch secret using builder") {
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

    should("throw exception when reading sub keys of non-existing secret") {
        val path = randomString()
        shouldThrow<VaultAPIException> {
            kv2.readSecretSubKeys(path)
        }
    }

    should("throw exception when reading sub keys of secret non-existing version") {
        val path = randomString()
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())

        shouldThrow<VaultAPIException> {
            kv2.readSecretSubKeys(path) {
                version = 2
            }
        }
    }

    should("read sub keys with default values") {
        val path = randomString()

        val writeGiven = readJson<KvV2WriteRequest>("cases/engine/kv/v2/read_sub_keys/without_options/given.json")
        val writeResponse = kv2.createOrUpdateSecret(path, writeGiven)

        val readResponse = kv2.readSecretSubKeys(path)
        val expected =
            readSubKeysResponse("cases/engine/kv/v2/read_sub_keys/without_options/expected.json", writeResponse)
        readResponse shouldBe expected
    }

    should("read sub keys with all defined values") {
        readSecretSubKeysWithOption(
            kv2,
            "cases/engine/kv/v2/read_sub_keys/with_options/given_1.json",
            "cases/engine/kv/v2/read_sub_keys/with_options/given_2.json",
            "cases/engine/kv/v2/read_sub_keys/with_options/parameters.json",
            "cases/engine/kv/v2/read_sub_keys/with_options/expected.json",
            kv2::readSecretSubKeys
        )
    }

    should("read secret sub keys using builder") {
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

    should("delete latest version without secret") {
        val path = randomString()
        kv2.deleteSecretLatestVersion(path) shouldBe true
    }

    should("delete latest version with secret and unique version") {
        val path = randomString()
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.deleteSecretLatestVersion(path)
        kv2.readSecret(path).isDeleted() shouldBe true
    }

    should("delete latest version with secret and multiple versions") {
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

    should("delete secret with non-existing secret") {
        val path = randomString()
        kv2.deleteSecretVersions(path, List(5) { it.toLong() }) shouldBe true
    }

    should("delete secret with non-existing version") {
        val path = randomString()
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.deleteSecretVersions(path, listOf(10)) shouldBe true
        kv2.readSecret(path).isDeleted() shouldBe false
    }

    should("delete secret with existing secret and version") {
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

    should("undelete secret with non-existing secret") {
        val path = randomString()
        kv2.undeleteSecretVersions(path, listOf(1)) shouldBe true
    }

    should("undelete secret with non-existing version") {
        val path = randomString()
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.undeleteSecretVersions(path, listOf(10)) shouldBe true
        kv2.readSecret(path).isDeleted() shouldBe false
    }

    should("undelete secret with existing secret and version") {
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

    should("destroy secret with non-existing secret") {
        val path = randomString()
        kv2.destroySecretVersions(path, listOf(1)) shouldBe true
    }

    should("destroy secret with non-existing version") {
        val path = randomString()
        kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        kv2.destroySecretVersions(path, listOf(10)) shouldBe true
        kv2.readSecret(path).isDestroyed() shouldBe false
    }

    should("destroy secret with existing secret and version") {
        val path = randomString()
        val writeResponse1 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())
        val writeResponse2 = kv2.createOrUpdateSecret(path, simpleWriteRequestBuilder())

        kv2.destroySecretVersions(path, listOf(writeResponse1.version)) shouldBe true

        kv2.readSecret(path, writeResponse1.version).isDestroyed() shouldBe true
        kv2.readSecret(path, writeResponse2.version).isDestroyed() shouldBe false

        kv2.undeleteSecretVersions(path, listOf(writeResponse1.version)) shouldBe true
        kv2.readSecret(path, writeResponse1.version).isDestroyed() shouldBe true
    }

    should("throw exception when listing secrets for a non-existing folder") {
        val path = randomString()
        shouldThrow<VaultAPIException> {
            kv2.listSecrets(path)
        }
    }

    should("list secrets for an existing folder") {
        val path = randomString()
        val key1 = randomString()
        val key2 = randomString()

        kv2.createOrUpdateSecret("$path/$key1", simpleWriteRequestBuilder())
        kv2.createOrUpdateSecret("$path/$key2/test", simpleWriteRequestBuilder())

        val response = kv2.listSecrets(path)
        response shouldContainExactlyInAnyOrder listOf(key1, "$key2/")
    }

    should("throw exception when reading metadata of non-existing secret") {
        val path = randomString()
        shouldThrow<VaultAPIException> {
            kv2.readSecretMetadata(path)
        }
    }

    should("read secret metadata with existing secret") {
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

    should("create or update metadata with non-existing secret") {
        val path = randomString()
        val writeGiven =
            readJson<KvV2WriteMetadataRequest>("cases/engine/kv/v2/update_metadata/without_secret/given.json")
        kv2.createOrUpdateMetadata(path, writeGiven) shouldBe true

        val readResponse = kv2.readSecretMetadata(path)
        val expected =
            readJson<KvV2ReadMetadataResponse>("cases/engine/kv/v2/update_metadata/without_secret/expected.json")
        readResponse shouldBe replaceTemplateString(expected, readResponse)
    }

    should("create or update metadata with existing secret") {
        updateMetadataWithSecret(
            kv2,
            "cases/engine/kv/v2/update_metadata/with_secret/given.json",
            "cases/engine/kv/v2/update_metadata/with_secret/expected.json"
        ) { path, writeGiven ->
            kv2.createOrUpdateMetadata(path, writeGiven)
        }
    }

    should("create or update metadata from builder") {
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

    should("throw exception when patching metadata of non-existing secret") {
        val path = randomString()
        val writeGiven =
            readJson<KvV2WriteMetadataRequest>("cases/engine/kv/v2/update_metadata/without_secret/given.json")
        kv2.patchMetadata(path, writeGiven) shouldBe false

        shouldThrow<VaultAPIException> {
            kv2.readSecretMetadata(path)
        }
    }

    should("patch metadata with existing secret") {
        updateMetadataWithSecret(
            kv2,
            "cases/engine/kv/v2/update_metadata/with_secret/given.json",
            "cases/engine/kv/v2/update_metadata/with_secret/expected.json"
        ) { path, writeGiven ->
            kv2.patchMetadata(path, writeGiven)
        }
    }

    should("patch metadata from builder") {
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

    should("delete metadata and all versions with non-existing secret") {
        val path = randomString()
        kv2.deleteMetadataAndAllVersions(path) shouldBe true
    }

    should("delete metadata and all versions with existing secret") {
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

private suspend inline fun updateMetadataWithSecret(
    kv2: VaultKV2Engine,
    writeGivenPath: String,
    readExpectedPath: String,
    updateMetadata: (String, KvV2WriteMetadataRequest) -> Boolean
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

    val writeExpectedResponse = readJson<KvV2WriteResponse>(writeExpectedResponsePath)
    writeResponse shouldBe replaceTemplateString(writeExpectedResponse, writeResponse)

    val readResponse = kv2.readSecret(path)
    val expected = readSecretResponse(readExpectedResponsePath, writeResponse)
    readResponse shouldBe expected
}

private fun readSubKeysResponse(path: String, writeResponse: KvV2WriteResponse) =
    readJson<KvV2ReadSubkeysResponse>(path).let {
        it.copy(
            metadata = it.metadata.copy(
                createdTime = writeResponse.createdTime,
                deletionTime = writeResponse.deletionTime
            )
        )
    }

private fun readSecretResponse(path: String, writeResponse: KvV2WriteResponse) = readJson<KvV2ReadResponse>(path).let {
    it.copy(
        metadata = it.metadata.copy(
            createdTime = writeResponse.createdTime,
            deletionTime = writeResponse.deletionTime
        )
    )
}

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
