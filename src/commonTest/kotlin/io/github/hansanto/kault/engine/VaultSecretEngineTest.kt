package io.github.hansanto.kault.engine

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.approle.VaultAuthAppRoleImpl
import io.github.hansanto.kault.engine.VaultSecretEngine
import io.github.hansanto.kault.engine.kv.v2.VaultKV2Engine
import io.github.hansanto.kault.engine.kv.v2.VaultKV2EngineImpl
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2ConfigureRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2SubKeysRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2WriteMetadataRequest
import io.github.hansanto.kault.engine.kv.v2.payload.KvV2WriteRequest
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadConfigurationResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadMetadataResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2ReadSubkeysResponse
import io.github.hansanto.kault.engine.kv.v2.response.KvV2WriteResponse
import io.github.hansanto.kault.engine.kv.v2.simpleWriteRequestBuilder
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.hours

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
