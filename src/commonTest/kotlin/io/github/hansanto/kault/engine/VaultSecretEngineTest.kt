package io.github.hansanto.kault.engine

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.engine.kv.v2.VaultKV2EngineImpl
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class VaultSecretEngineTest {

    lateinit var client: VaultClient

    @BeforeTest
    fun onBefore() = runTest {
        client = createVaultClient()
    }

    @AfterTest
    fun onAfter() = runTest {
        client.close()
    }

    @Test
    fun `should use default path if not set in builder`() = runTest {
        val built = VaultSecretEngine(client.client, null) {
        }

        (built.kv2 as VaultKV2EngineImpl).path shouldBe VaultKV2EngineImpl.Default.PATH
    }

    @Test
    fun `should use custom path if set in builder`() = runTest {
        val kv2Path = randomString()
        val parentPath = randomString()

        val built = VaultSecretEngine(client.client, parentPath) {
            kv2 {
                path = kv2Path
            }
        }

        (built.kv2 as VaultKV2EngineImpl).path shouldBe "$parentPath/$kv2Path"
    }
}
