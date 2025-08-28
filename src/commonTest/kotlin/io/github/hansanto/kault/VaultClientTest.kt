package io.github.hansanto.kault

import io.github.hansanto.kault.util.VAULT_URL
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.revokeAllTokenData
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.isActive

class VaultClientTest :
    ShouldSpec({

        afterTest {
            createVaultClient().use {
                revokeAllTokenData(it)
            }
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
    })
