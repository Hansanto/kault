package io.github.hansanto.kault

import io.github.hansanto.kault.engine.kv.v2.createOrUpdateSecret
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.system.mounts.response.MountsGetConfigurationOfSecretEngineResponse
import io.github.hansanto.kault.util.VAULT_URL
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.createVaultEnterpriseClient
import io.github.hansanto.kault.util.deleteAllKV2Secrets
import io.github.hansanto.kault.util.deleteAllNamespaces
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.revokeAllTokenData
import io.github.hansanto.kault.util.toMountsEnableSecretsEnginePayload
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.isActive

private const val SECRET_ENGINE = "secret"

class VaultClientTest :
    ShouldSpec({

        lateinit var defaultKV2Configuration: MountsGetConfigurationOfSecretEngineResponse
        lateinit var client: VaultClient
        lateinit var enterpriseClient: VaultClient

        beforeTest {
            client = createVaultClient()
            enterpriseClient = createVaultEnterpriseClient()
            defaultKV2Configuration = client.system.mounts.getConfigurationOfSecretEngine(SECRET_ENGINE)
        }

        afterTest {
            createVaultClient().use {
                revokeAllTokenData(it)
            }
            deleteAllNamespaces(enterpriseClient)
            deleteAllKV2Secrets(enterpriseClient)
            enterpriseClient.close()
        }

        should("use default values if not set in builder") {
            val built = VaultClient {
                url = VAULT_URL
            }
            built.auth.autoRenewToken shouldBe true
        }

        should("use custom values in the builder") {
            val url = randomString()
            val namespace = randomString()
            val autoRenewToken = randomBoolean()

            val built = VaultClient {
                this.url = url
                this.namespace = namespace
                auth {
                    this.autoRenewToken = autoRenewToken
                }
            }

            built.auth.autoRenewToken shouldBe autoRenewToken
        }

        should("cancel coroutine scope when closing client") {
            createVaultClient().use {
                it.client.coroutineContext.isActive shouldBe true
                it.close()
                it.client.coroutineContext.isActive shouldBe false
            }
        }

        should("use namespace if set in builder") {
            val namespace1 = randomString()
            val namespace2 = randomString()

            val secretPath = "path"
            enterpriseClient.system.namespaces.create(namespace1)
            val dataNamespace1 = mapOf("key1" to "value1")
            enterpriseClient.system.namespaces.create(namespace2)
            val dataNamespace2 = mapOf("key2" to "value2")

            val enableSecretsEnginePayload = defaultKV2Configuration.toMountsEnableSecretsEnginePayload()

            createVaultEnterpriseClient(namespace = namespace1).use { clientNamespace1 ->
                clientNamespace1.system.mounts.enableSecretsEngine(SECRET_ENGINE, enableSecretsEnginePayload)

                clientNamespace1.secret.kv2.createOrUpdateSecret(secretPath) {
                    this.data(dataNamespace1)
                }

                createVaultEnterpriseClient(namespace = namespace2).use { clientNamespace2 ->
                    clientNamespace2.system.mounts.enableSecretsEngine(SECRET_ENGINE, enableSecretsEnginePayload)
                    shouldThrow<VaultAPIException> {
                        clientNamespace2.secret.kv2.readSecret(secretPath)
                    }
                    clientNamespace2.secret.kv2.createOrUpdateSecret(secretPath) {
                        this.data(dataNamespace2)
                    }
                    clientNamespace2.secret.kv2.readSecret(secretPath).data<Map<String, String>>() shouldBe
                        dataNamespace2
                }

                clientNamespace1.secret.kv2.readSecret(secretPath).data<Map<String, String>>() shouldBe dataNamespace1
            }

            shouldThrow<VaultAPIException> {
                enterpriseClient.secret.kv2.readSecret(secretPath)
            }
        }
    })
