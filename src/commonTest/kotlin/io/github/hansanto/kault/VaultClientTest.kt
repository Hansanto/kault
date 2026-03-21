package io.github.hansanto.kault

import io.github.hansanto.kault.engine.kv.v2.createOrUpdateSecret
import io.github.hansanto.kault.util.VAULT_URL
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.createVaultEnterpriseClient
import io.github.hansanto.kault.util.deleteAllKV2Secrets
import io.github.hansanto.kault.util.deleteAllNamespaces
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.revokeAllTokenData
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.isActive

class VaultClientTest :
    ShouldSpec({

        lateinit var enterpriseClient: VaultClient

        beforeTest {
            enterpriseClient = createVaultEnterpriseClient()
        }

        afterTest {
            createVaultClient().use {
                revokeAllTokenData(it)
            }
            deleteAllNamespaces(enterpriseClient)
            enterpriseClient.close()
            deleteAllKV2Secrets(enterpriseClient)
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

            enterpriseClient.system.namespaces.create(namespace1)
            enterpriseClient.system.namespaces.create(namespace2)

            // TODO: Need to implement https://developer.hashicorp.com/vault/api-docs/system/mounts
            //  to support creating secrets in different namespaces without creating multiple clients

            createVaultEnterpriseClient(namespace = namespace1).use { clientNamespace1 ->
                clientNamespace1.secret.kv2.createOrUpdateSecret("path") {
                    this.data(mapOf("key1" to "value1"))
                }

                createVaultEnterpriseClient(namespace = namespace2).use { clientNamespace2 ->
                    clientNamespace2.secret.kv2.readSecret("path") shouldBe null
                    clientNamespace2.secret.kv2.createOrUpdateSecret("path") {
                        this.data(mapOf("key2" to "value2"))
                    }
                }

                clientNamespace1.secret.kv2.readSecret("path") shouldBe mapOf("key1" to "value1")
            }

            enterpriseClient.secret.kv2.readSecret("path") shouldBe null
        }
    })
