package io.github.hansanto.kault.engine

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.engine.kv.v2.VaultKV2EngineImpl
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VaultKV2EngineTest : FunSpec({

    lateinit var client: VaultClient

    beforeSpec {
        client = createVaultClient()
    }

    afterSpec {
        client.close()
    }

    test("builder should set values correctly") {
        val kv2Path = randomString()

        val built = VaultSecretEngine(client.client) {
            kv2 {
                path = kv2Path
            }
        }

        (built.kv2 as VaultKV2EngineImpl).path shouldBe kv2Path
    }
})
