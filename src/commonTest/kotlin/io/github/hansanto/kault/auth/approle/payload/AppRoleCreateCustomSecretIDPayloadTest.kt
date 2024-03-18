package io.github.hansanto.kault.auth.approle.payload

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class AppRoleCreateCustomSecretIDPayloadTest : FunSpec({

    test("metadata from empty map") {
        val payload = AppRoleCreateCustomSecretIDPayload("")
        payload.metadata(emptyMap())
        payload.metadata shouldBe "{}"
    }

    test("metadata from map with one entry") {
        val payload = AppRoleCreateCustomSecretIDPayload("")
        payload.metadata(mapOf("key" to "value"))
        payload.metadata shouldBe "{\"key\":\"value\"}"
    }

    test("metadata from map with multiple entries") {
        val payload = AppRoleCreateCustomSecretIDPayload("")
        payload.metadata(mapOf("key1" to "value1", "key2" to "value2"))
        payload.metadata shouldBe "{\"key1\":\"value1\",\"key2\":\"value2\"}"
    }

    test("builder metadata from empty map") {
        val builder = AppRoleCreateCustomSecretIDPayload
            .Builder().apply {
                metadata(emptyMap())
            }
        builder.metadata shouldBe "{}"
    }

    test("builder metadata from map with one entry") {
        val builder = AppRoleCreateCustomSecretIDPayload
            .Builder().apply {
                metadata(mapOf("key" to "value"))
            }
        builder.metadata shouldBe "{\"key\":\"value\"}"
    }

    test("builder metadata from map with multiple entries") {
        val builder = AppRoleCreateCustomSecretIDPayload
            .Builder().apply {
                metadata(mapOf("key1" to "value1", "key2" to "value2"))
            }
        builder.metadata shouldBe "{\"key1\":\"value1\",\"key2\":\"value2\"}"
    }
})
