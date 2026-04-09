package io.github.hansanto.kault.system.mounts

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.system.mounts.payload.MountsEnableSecretsEnginePayload
import io.github.hansanto.kault.system.mounts.payload.MountsTuneConfigurationPayload
import io.github.hansanto.kault.system.mounts.response.MountsGetConfigurationOfSecretEngineResponse
import io.github.hansanto.kault.system.mounts.response.MountsListMountedSecretsEnginesResponse
import io.github.hansanto.kault.system.mounts.response.MountsReadConfigurationResponse
import io.github.hansanto.kault.util.createVaultEnterpriseClient
import io.github.hansanto.kault.util.deleteAllNamespaces
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.replaceTemplateString
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

private const val DEFAULT_ENGINE = "kv"

class VaultSystemMountsTest :
    ShouldSpec({

        lateinit var client: VaultClient
        lateinit var mounts: VaultSystemMounts
        lateinit var clientNamespace: VaultClient
        lateinit var mountsNamespace: VaultSystemMounts

        beforeSpec {
            client = createVaultEnterpriseClient()
            mounts = client.system.mounts
        }

        beforeTest {
            val namespace = randomString()

            client.system.namespaces.create(namespace)
            clientNamespace = createVaultEnterpriseClient(namespace)
            mountsNamespace = clientNamespace.system.mounts
        }

        afterTest {
            deleteAllNamespaces(client)
            clientNamespace.close()
        }

        afterSpec {
            client.close()
        }

        should("use default path if not set in builder") {
            VaultSystemMountsImpl.Default.PATH shouldBe "mounts"

            val built = VaultSystemMountsImpl.Companion(client.client, null) {
            }

            built.path shouldBe VaultSystemMountsImpl.Default.PATH
        }

        should("use custom values in the builder") {
            val randomPath = randomString()
            val parentPath = randomString()

            val built = VaultSystemMountsImpl.Companion(client.client, parentPath) {
                path = randomPath
            }

            built.path shouldBe "$parentPath/$randomPath"
        }

        should("list no additional mounted secrets engines") {
            val actual = mounts.listMountedSecretsEngines()
            actual shouldBe replaceTemplateString(
                expected = readJson<MountsListMountedSecretsEnginesResponse>(
                    "cases/system/mounts/list_mounted_secrets_engines/without_options/expected.json"
                ),
                response = actual,
            )
        }

        should("list with additional mounted secrets engines") {
            val given =
                readJson<MountsEnableSecretsEnginePayload>(
                    "cases/system/mounts/list_mounted_secrets_engines/with_options/given.json"
                )
            mountsNamespace.enableSecretsEngine(DEFAULT_ENGINE, given)

            val actual = mountsNamespace.listMountedSecretsEngines()
            actual shouldBe replaceTemplateString(
                expected = readJson<MountsListMountedSecretsEnginesResponse>(
                    "cases/system/mounts/list_mounted_secrets_engines/with_options/expected.json"
                ),
                response = actual,
            )
        }

        should("throw exception when enabling non-existing engine with empty payload") {
            val exception = shouldThrow<Exception> {
                mountsNamespace.enableSecretsEngine(DEFAULT_ENGINE) {}
            }

            (exception !is VaultAPIException) shouldBe true

            shouldThrow<VaultAPIException> {
                mountsNamespace.readMountConfiguration(DEFAULT_ENGINE)
            }
        }

        should("get configuration of default secret engine configuration") {
            // Used to check nullable fields are handled correctly
            shouldNotThrowAny {
                mounts.getConfigurationOfSecretEngine("cubbyhole")
                mounts.getConfigurationOfSecretEngine("identity")
                mounts.getConfigurationOfSecretEngine("sys")
                mounts.getConfigurationOfSecretEngine("secret")
            }
        }

        should("enable secrets engine with minimal payload") {
            assertEnableSecretsEngine(
                mounts = mountsNamespace,
                path = DEFAULT_ENGINE,
                givenPath = "cases/system/mounts/enable_secrets_engine/without_options/given.json",
                expectedPath = "cases/system/mounts/enable_secrets_engine/without_options/expected.json"
            )
        }

        should("enable secrets engine with minimal payload using builder") {
            assertEnableSecretsEngineWithBuilder(
                mounts = mountsNamespace,
                path = DEFAULT_ENGINE,
                givenPath = "cases/system/mounts/enable_secrets_engine/without_options/given.json",
                expectedPath = "cases/system/mounts/enable_secrets_engine/without_options/expected.json"
            )
        }

        should("enable secrets engine with all defined values") {
            assertEnableSecretsEngine(
                mounts = mountsNamespace,
                path = DEFAULT_ENGINE,
                givenPath = "cases/system/mounts/enable_secrets_engine/with_options/given.json",
                expectedPath = "cases/system/mounts/enable_secrets_engine/with_options/expected.json"
            )
        }

        should("enable secrets engine with all defined values using builder") {
            assertEnableSecretsEngineWithBuilder(
                mounts = mountsNamespace,
                path = DEFAULT_ENGINE,
                givenPath = "cases/system/mounts/enable_secrets_engine/with_options/given.json",
                expectedPath = "cases/system/mounts/enable_secrets_engine/with_options/expected.json"
            )
        }

        should("disable secrets engine return true if path does not exist") {
            val result = mountsNamespace.disableSecretsEngine("non-existing-path")
            result shouldBe true
        }

        should("disable secrets engine return true if path exists") {
            mountsNamespace.enableSecretsEngine(DEFAULT_ENGINE) {
                type = DEFAULT_ENGINE
                kvOptions(2)
            } shouldBe true

            shouldNotThrowAny {
                mountsNamespace.getConfigurationOfSecretEngine(DEFAULT_ENGINE)
            }

            mountsNamespace.disableSecretsEngine(DEFAULT_ENGINE) shouldBe true
            shouldThrow<VaultAPIException> {
                mountsNamespace.getConfigurationOfSecretEngine(DEFAULT_ENGINE)
            }
        }

        should("read mount configuration of default secret engine") {
            // Used to check nullable fields are handled correctly
            shouldNotThrowAny {
                mounts.readMountConfiguration("cubbyhole")
                mounts.readMountConfiguration("identity")
                mounts.readMountConfiguration("sys")
                mounts.readMountConfiguration("secret")
            }
        }

        should("tune mount configuration with minimal payload") {
            assertTuneMountConfiguration(
                mounts = mountsNamespace,
                path = DEFAULT_ENGINE,
                createPath = null,
                givenPath = null,
                expectedPath = "cases/system/mounts/tune_mount_configuration/without_options/expected.json"
            )
        }

        should("tune mount configuration with minimal payload using builder") {
            assertTuneMountConfigurationWithBuilder(
                mounts = mountsNamespace,
                path = DEFAULT_ENGINE,
                createPath = null,
                givenPath = null,
                expectedPath = "cases/system/mounts/tune_mount_configuration/without_options/expected.json"
            )
        }

        should("tune mount configuration with all defined values") {
            assertTuneMountConfiguration(
                mounts = mountsNamespace,
                path = DEFAULT_ENGINE,
                createPath = "cases/system/mounts/tune_mount_configuration/with_options/given_create.json",
                givenPath = "cases/system/mounts/tune_mount_configuration/with_options/given_update.json",
                expectedPath = "cases/system/mounts/tune_mount_configuration/with_options/expected.json"
            )
        }

        should("tune mount configuration with all defined values using builder") {
            assertTuneMountConfigurationWithBuilder(
                mounts = mountsNamespace,
                path = DEFAULT_ENGINE,
                createPath = "cases/system/mounts/tune_mount_configuration/with_options/given_create.json",
                givenPath = "cases/system/mounts/tune_mount_configuration/with_options/given_update.json",
                expectedPath = "cases/system/mounts/tune_mount_configuration/with_options/expected.json"
            )
        }
    })

private suspend fun assertEnableSecretsEngine(
    mounts: VaultSystemMounts,
    path: String,
    givenPath: String?,
    expectedPath: String
) {
    assertEnableSecretsEngine(mounts, path, givenPath, expectedPath) { payload ->
        mounts.enableSecretsEngine(path, payload)
    }
}

private suspend fun assertEnableSecretsEngineWithBuilder(
    mounts: VaultSystemMounts,
    path: String,
    givenPath: String?,
    expectedPath: String
) {
    assertEnableSecretsEngine(mounts, path, givenPath, expectedPath) { payload ->
        mounts.enableSecretsEngine(path) {
            type = payload.type
            description = payload.description
            val payloadConfig = payload.config
            if (payloadConfig != null) {
                config {
                    defaultLeaseTTL = payloadConfig.defaultLeaseTTL
                    maxLeaseTTL = payloadConfig.maxLeaseTTL
                    forceNoCache = payloadConfig.forceNoCache
                    auditNonHmacRequestKeys = payloadConfig.auditNonHmacRequestKeys
                    auditNonHmacResponseKeys = payloadConfig.auditNonHmacResponseKeys
                    listingVisibility = payloadConfig.listingVisibility
                    passthroughRequestHeaders = payloadConfig.passthroughRequestHeaders
                    allowedResponseHeaders = payloadConfig.allowedResponseHeaders
                    allowedManagedKeys = payloadConfig.allowedManagedKeys
                    delegatedAuthAccessors = payloadConfig.delegatedAuthAccessors
                    identityTokenKey = payloadConfig.identityTokenKey
                }
            }
            options = payload.options
            local = payload.local
            sealWrap = payload.sealWrap
            externalEntropyAccess = payload.externalEntropyAccess
        }
    }
}

private suspend inline fun assertEnableSecretsEngine(
    mounts: VaultSystemMounts,
    path: String,
    givenPath: String?,
    expectedPath: String,
    enableSecretsEngine: (MountsEnableSecretsEnginePayload) -> Boolean
) {
    val payload = givenPath?.let { readJson<MountsEnableSecretsEnginePayload>(it) }
        ?: MountsEnableSecretsEnginePayload(path)

    enableSecretsEngine(payload) shouldBe true
    val actual = mounts.getConfigurationOfSecretEngine(path)
    actual shouldBe replaceTemplateString(
        response = actual,
        expected = readJson<MountsGetConfigurationOfSecretEngineResponse>(expectedPath)
    )
}

private suspend fun assertTuneMountConfiguration(
    mounts: VaultSystemMounts,
    path: String,
    createPath: String?,
    givenPath: String?,
    expectedPath: String
) {
    assertTuneMountConfiguration(mounts, path, createPath, givenPath, expectedPath) { path, payload ->
        mounts.tuneMountConfiguration(path, payload)
    }
}

private suspend fun assertTuneMountConfigurationWithBuilder(
    mounts: VaultSystemMounts,
    path: String,
    createPath: String?,
    givenPath: String?,
    expectedPath: String
) {
    assertTuneMountConfiguration(mounts, path, createPath, givenPath, expectedPath) { path, payload ->
        mounts.tuneMountConfiguration(path) {
            defaultLeaseTTL = payload.defaultLeaseTTL
            maxLeaseTTL = payload.maxLeaseTTL
            description = payload.description
            auditNonHmacRequestKeys = payload.auditNonHmacRequestKeys
            auditNonHmacResponseKeys = payload.auditNonHmacResponseKeys
            listingVisibility = payload.listingVisibility
            passthroughRequestHeaders = payload.passthroughRequestHeaders
            allowedResponseHeaders = payload.allowedResponseHeaders
            allowedManagedKeys = payload.allowedManagedKeys
            pluginVersion = payload.pluginVersion
            delegatedAuthAccessors = payload.delegatedAuthAccessors
        }
    }
}

private suspend inline fun assertTuneMountConfiguration(
    mounts: VaultSystemMounts,
    path: String,
    createPath: String?,
    givenPath: String?,
    expectedPath: String,
    tuneMountConfiguration: (String, MountsTuneConfigurationPayload) -> Boolean
) {
    val createPayload = createPath?.let { readJson<MountsEnableSecretsEnginePayload>(it) }
        ?: MountsEnableSecretsEnginePayload(path)

    mounts.enableSecretsEngine(path, createPayload) shouldBe true

    val updateConfiguration = givenPath?.let { readJson<MountsTuneConfigurationPayload>(it) }
        ?: MountsTuneConfigurationPayload()

    tuneMountConfiguration(path, updateConfiguration) shouldBe true
    val actual = mounts.readMountConfiguration(path)
    actual shouldBe replaceTemplateString(
        response = actual,
        expected = readJson<MountsReadConfigurationResponse>(expectedPath)
    )
}
