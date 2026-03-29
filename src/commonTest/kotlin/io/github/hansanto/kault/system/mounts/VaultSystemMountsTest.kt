package io.github.hansanto.kault.system.mounts

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.system.mounts.payload.MountsEnableSecretsEnginePayload
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
        lateinit var initialConfiguration: MountsReadConfigurationResponse

//        beforeSpec {
//            createVaultEnterpriseClient().use {
//                initialConfiguration = it.system.mounts.readMountConfiguration(DEFAULT_ENGINE)
//            }
//        }

        beforeTest {
            val namespace = randomString()
            client = createVaultEnterpriseClient()
            mounts = client.system.mounts

            client.system.namespaces.create(namespace)
            clientNamespace = createVaultEnterpriseClient(namespace)
            mountsNamespace = clientNamespace.system.mounts
        }

        afterTest {
            deleteAllNamespaces(client)
            client.close()
            clientNamespace.close()
        }

//        afterSpec {
//            createVaultEnterpriseClient().use {
//                it.system.mounts.disableSecretsEngine(DEFAULT_ENGINE)
//                it.system.mounts.enableSecretsEngine(DEFAULT_ENGINE) {
//                    type = DEFAULT_ENGINE
//                    description = initialConfiguration.description
//                    config {
//                        defaultLeaseTTL = initialConfiguration.defaultLeaseTTL
//                        maxLeaseTTL = initialConfiguration.maxLeaseTTL
//                        forceNoCache = initialConfiguration.forceNoCache
//                        auditNonHmacRequestKeys = initialConfiguration.auditNonHmacRequestKeys
//                        auditNonHmacResponseKeys = initialConfiguration.auditNonHmacResponseKeys
//                        listingVisibility = initialConfiguration.listingVisibility
//                        passthroughRequestHeaders = initialConfiguration.passthroughRequestHeaders
//                        allowedResponseHeaders = initialConfiguration.allowedResponseHeaders
//                        allowedManagedKeys = initialConfiguration.allowedManagedKeys
//                        delegatedAuthAccessors = initialConfiguration.delegatedAuthAccessors
//                        identityTokenKey = initialConfiguration.identityTokenKey
//                    }
//                    options = initialConfiguration.options
//                    local = initialConfiguration.local
//                    sealWrap = initialConfiguration.sealWrap
//                    externalEntropyAccess = initialConfiguration.externalEntropyAccess
//                }
//            }
//        }

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

        should("list default mounted secrets engines") {
            val actual = mountsNamespace.listMountedSecretsEngines()
            actual shouldBe replaceTemplateString(
                expected = readJson<MountsListMountedSecretsEnginesResponse>(
                    "cases/sys/mounts/list_mounted_secrets_engines/expected.json"
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

        should("read default engine configuration") {
            // Used to check nullable fields are handled correctly
            shouldNotThrowAny {
                mounts.readMountConfiguration("cubbyhole")
                mounts.readMountConfiguration("identity")
                mounts.readMountConfiguration("sys")
                mounts.readMountConfiguration("secret")
            }
        }

        should("enable engine with minimal payload") {
            assertEnableSecretsEngine(
                mounts = mountsNamespace,
                givenPath = "cases/sys/mounts/enable_secrets_engine/without_options/given.json",
                expectedPath = "cases/sys/mounts/enable_secrets_engine/without_options/expected.json"
            )
        }

        should("enable engine with minimal payload using builder") {
            assertEnableSecretsEngineWithBuilder(
                mounts = mountsNamespace,
                givenPath = "cases/sys/mounts/enable_secrets_engine/without_options/given.json",
                expectedPath = "cases/sys/mounts/enable_secrets_engine/without_options/expected.json"
            )
        }

        should("enable engine with all defined values") {
            assertEnableSecretsEngine(
                mounts = mountsNamespace,
                givenPath = "cases/sys/mounts/enable_secrets_engine/with_options/given.json",
                expectedPath = "cases/sys/mounts/enable_secrets_engine/with_options/expected.json"
            )
        }

        should("enable engine with all defined values using builder") {
            assertEnableSecretsEngineWithBuilder(
                mounts = mountsNamespace,
                givenPath = "cases/sys/mounts/enable_secrets_engine/with_options/given.json",
                expectedPath = "cases/sys/mounts/enable_secrets_engine/with_options/expected.json"
            )
        }
    })

private suspend fun assertEnableSecretsEngine(mounts: VaultSystemMounts, givenPath: String?, expectedPath: String) {
    assertEnableSecretsEngine(mounts, givenPath, expectedPath) { payload ->
        mounts.enableSecretsEngine(DEFAULT_ENGINE, payload)
    }
}

private suspend fun assertEnableSecretsEngineWithBuilder(
    mounts: VaultSystemMounts,
    givenPath: String?,
    expectedPath: String
) {
    assertEnableSecretsEngine(mounts, givenPath, expectedPath) { payload ->
        mounts.enableSecretsEngine(DEFAULT_ENGINE) {
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
    givenPath: String?,
    expectedPath: String,
    enableSecretsEngine: (MountsEnableSecretsEnginePayload) -> Boolean
) {
    val payload = givenPath?.let { readJson<MountsEnableSecretsEnginePayload>(it) }
        ?: MountsEnableSecretsEnginePayload(DEFAULT_ENGINE)

    enableSecretsEngine(payload) shouldBe true
    mounts.readMountConfiguration(DEFAULT_ENGINE) shouldBe readJson<MountsReadConfigurationResponse>(expectedPath)
}
