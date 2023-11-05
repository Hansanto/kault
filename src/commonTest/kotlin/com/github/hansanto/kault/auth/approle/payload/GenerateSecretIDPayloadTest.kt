package com.github.hansanto.kault.auth.approle.payload

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class GenerateSecretIDPayloadTest : FunSpec({

    test("metadata from empty map") {
        val payload = GenerateSecretIDPayload()
        payload.metadata(emptyMap())
        payload.metadata shouldBe "{}"
    }

    test("metadata from map with one entry") {
        val payload = GenerateSecretIDPayload()
        payload.metadata(mapOf("key" to "value"))
        payload.metadata shouldBe "{\"key\":\"value\"}"
    }

    test("metadata from map with multiple entries") {
        val payload = GenerateSecretIDPayload()
        payload.metadata(mapOf("key1" to "value1", "key2" to "value2"))
        payload.metadata shouldBe "{\"key1\":\"value1\",\"key2\":\"value2\"}"
    }
})
