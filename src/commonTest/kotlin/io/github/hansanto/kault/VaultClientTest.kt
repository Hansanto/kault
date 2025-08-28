package io.github.hansanto.kault

import io.github.hansanto.kault.util.VAULT_URL
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.revokeAllTokenData
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.isActive
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class VaultClientTest {

    @AfterTest
    fun onAfter() = runTest {
        createVaultClient().use {
            revokeAllTokenData(it)
        }
    }

    @Test
    fun `should use default values if not set in builder`() = runTest {
        val built = VaultClient {
            url = VAULT_URL
        }
        built.auth.autoRenewToken shouldBe true
    }

    @Test
    fun `should use custom values in the builder`() = runTest {
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

    @Test
    fun `should cancel coroutine scope when closing client`() = runTest {
        createVaultClient().use {
            it.client.coroutineContext.isActive shouldBe true
            it.close()
            it.client.coroutineContext.isActive shouldBe false
        }
    }
}
