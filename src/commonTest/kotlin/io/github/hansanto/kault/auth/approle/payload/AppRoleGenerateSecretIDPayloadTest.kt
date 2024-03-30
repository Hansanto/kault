package io.github.hansanto.kault.auth.approle.payload

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class AppRoleGenerateSecretIDPayloadTest : ShouldSpec({

    should("transform metadata empty map to empty json") {
        val payload = AppRoleGenerateSecretIDPayload()
        payload.metadata(emptyMap())
        payload.metadata shouldBe "{}"
    }

    should("transform metadata map with one entry to json") {
        val payload = AppRoleGenerateSecretIDPayload()
        payload.metadata(mapOf("key" to "value"))
        payload.metadata shouldBe "{\"key\":\"value\"}"
    }

    should("transform metadata from map with multiple entries") {
        val payload = AppRoleGenerateSecretIDPayload()
        payload.metadata(mapOf("key1" to "value1", "key2" to "value2"))
        payload.metadata shouldBe "{\"key1\":\"value1\",\"key2\":\"value2\"}"
    }
})
