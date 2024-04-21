package io.github.hansanto.kault

import io.github.hansanto.kault.util.createVaultClient
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.use
import kotlinx.coroutines.isActive

class VaultClientTest : ShouldSpec({

    should("cancel coroutine scope when closing client") {
        createVaultClient().use {
            it.client.coroutineContext.isActive shouldBe true
            it.close()
            it.client.coroutineContext.isActive shouldBe false
        }
    }
})
