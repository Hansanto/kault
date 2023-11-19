package io.github.hansanto.kault.engine.kv.v2

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2ConfigureRequest
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadConfigurationResponse
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.readJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VaultKV2EngineTest : FunSpec({

    lateinit var client: VaultClient
    lateinit var kv2: VaultKV2Engine

    beforeSpec {
        client = createVaultClient()
        kv2 = client.secret.kv2
    }

    afterSpec {
        client.close()
    }

    test("configure without options") {
        kv2.configure() shouldBe true
        val response = kv2.readConfiguration()
        val expected = readJson<KvV2ReadConfigurationResponse>("cases/engine/kv/v2/configure/without_options/expected.json")
        response shouldBe expected
    }

    test("configure with options") {
        val given = readJson<KvV2ConfigureRequest>("cases/engine/kv/v2/configure/with_options/expected.json")
        kv2.configure(given) shouldBe true
        val response = kv2.readConfiguration()
        val expected = readJson<KvV2ReadConfigurationResponse>("cases/engine/kv/v2/configure/with_options/expected.json")
        response shouldBe expected
    }
})
