package io.github.hansanto.kault

import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomBoolean
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.use
import kotlinx.coroutines.isActive

class VaultClientTest : ShouldSpec({

    should("use default values if not set in builder") {
        val built = VaultClient {
            url = "http://localhost:8200"
        }
        built.auth.autoRenewToken shouldBe true
    }

    should("use custom values in the builder") {
        val autoRenewToken = randomBoolean()

        val built = VaultClient {
            url = "http://localhost:8200"
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
