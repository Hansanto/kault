package com.github.hansanto.kault.system.auth.payload

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EnableMethodPayloadTest : FunSpec({

    test("options from empty map") {
        val payload = EnableMethodPayload("")
        payload.options(emptyMap())
        payload.options shouldBe "{}"
    }

    test("options from map with one entry") {
        val payload = EnableMethodPayload("")
        payload.options(mapOf("key" to "value"))
        payload.options shouldBe "{\"key\":\"value\"}"
    }

    test("options from map with multiple entries") {
        val payload = EnableMethodPayload("")
        payload.options(mapOf("key1" to "value1", "key2" to "value2"))
        payload.options shouldBe "{\"key1\":\"value1\",\"key2\":\"value2\"}"
    }

    test("builder options from empty map") {
        val builder = EnableMethodPayload
            .Builder().apply {
                options(emptyMap())
            }
        builder.options shouldBe "{}"
    }

    test("builder options from map with one entry") {
        val builder = EnableMethodPayload
            .Builder().apply {
                options(mapOf("key" to "value"))
            }
        builder.options shouldBe "{\"key\":\"value\"}"
    }

    test("builder options from map with multiple entries") {
        val builder = EnableMethodPayload
            .Builder().apply {
                options(mapOf("key1" to "value1", "key2" to "value2"))
            }
        builder.options shouldBe "{\"key1\":\"value1\",\"key2\":\"value2\"}"
    }
})
