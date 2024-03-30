package io.github.hansanto.kault.auth.approle.payload

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class AppRoleCreateCustomSecretIDPayloadTest : ShouldSpec({

    should("transform metadata empty map to empty json") {
        val payload = AppRoleCreateCustomSecretIDPayload("")
        payload.metadata(emptyMap())
        payload.metadata shouldBe "{}"
    }

    should("transform metadata map with one entry to json") {
        val payload = AppRoleCreateCustomSecretIDPayload("")
        payload.metadata(mapOf("key" to "value"))
        payload.metadata shouldBe "{\"key\":\"value\"}"
    }

    should("transform metadata from map with multiple entries") {
        val payload = AppRoleCreateCustomSecretIDPayload("")
        payload.metadata(mapOf("key1" to "value1", "key2" to "value2"))
        payload.metadata shouldBe "{\"key1\":\"value1\",\"key2\":\"value2\"}"
    }

    should("transform builder metadata empty map to empty json") {
        val builder = AppRoleCreateCustomSecretIDPayload
            .Builder().apply {
                metadata(emptyMap())
            }
        builder.metadata shouldBe "{}"
    }

    should("transform builder metadata map with one entry to json") {
        val builder = AppRoleCreateCustomSecretIDPayload
            .Builder().apply {
                metadata(mapOf("key" to "value"))
            }
        builder.metadata shouldBe "{\"key\":\"value\"}"
    }

    should("transform builder metadata from map with multiple entries") {
        val builder = AppRoleCreateCustomSecretIDPayload
            .Builder().apply {
                metadata(mapOf("key1" to "value1", "key2" to "value2"))
            }
        builder.metadata shouldBe "{\"key1\":\"value1\",\"key2\":\"value2\"}"
    }
})
