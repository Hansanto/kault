package io.github.hansanto.kault.engine

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.engine.kv.v2.VaultKV2EngineImpl
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class VaultSecretEngineTest : ShouldSpec({

    lateinit var client: VaultClient

    beforeSpec {
        client = createVaultClient()
    }

    afterSpec {
        client.close()
    }

    should("use default path if not set in builder") {
        val built = VaultSecretEngine(client.client, null) {
        }

        (built.kv2 as VaultKV2EngineImpl).path shouldBe VaultKV2EngineImpl.Default.PATH
    }

    should("use custom path if set in builder") {
        val kv2Path = randomString()
        val parentPath = randomString()

        val built = VaultSecretEngine(client.client, parentPath) {
            kv2 {
                path = kv2Path
            }
        }

        (built.kv2 as VaultKV2EngineImpl).path shouldBe "$parentPath/$kv2Path"
    }
})
