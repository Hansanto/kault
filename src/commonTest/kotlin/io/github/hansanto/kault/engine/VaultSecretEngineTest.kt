package io.github.hansanto.kault.engine

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.engine.kv.v2.VaultKV2EngineImpl
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VaultSecretEngineTest : FunSpec({

    lateinit var client: VaultClient

    beforeSpec {
        client = createVaultClient()
    }

    afterSpec {
        client.close()
    }

    test("builder default variables should be set correctly") {
        val built = VaultSecretEngine(client.client, null) {
        }

        (built.kv2 as VaultKV2EngineImpl).path shouldBe VaultKV2EngineImpl.Default.PATH
    }

    test("builder should set values correctly") {
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
