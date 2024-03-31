package io.github.hansanto.kault.auth.approle.payload

import io.github.hansanto.kault.util.randomLong
import io.github.hansanto.kault.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds

class AppRoleCreateCustomSecretIDPayloadTest : ShouldSpec({

    should("throw exception when mandatory fields are not set using builder") {
        val builder = AppRoleCreateCustomSecretIDPayload.Builder()
            .apply {
                metadata = randomString()
                cidrList = List(5) { randomString() }
                tokenBoundCidrs = List(5) { randomString() }
                numUses = randomLong()
                ttl = randomLong().milliseconds
            }

        shouldThrow<Exception> {
            builder.build()
        }
    }

    should("create instance with only mandatory fields using builder") {
        val payload = AppRoleCreateCustomSecretIDPayload(
            secretId = randomString()
        )

        AppRoleCreateCustomSecretIDPayload.Builder()
            .apply {
                secretId = payload.secretId
            }
            .build() shouldBe payload
    }

    should("create instance with all fields using builder") {
        val payload = AppRoleCreateCustomSecretIDPayload(
            secretId = randomString(),
            metadata = randomString(),
            cidrList = List(5) { randomString() },
            tokenBoundCidrs = List(5) { randomString() },
            numUses = randomLong(),
            ttl = randomLong().milliseconds
        )

        AppRoleCreateCustomSecretIDPayload.Builder()
            .apply {
                secretId = payload.secretId
                metadata = payload.metadata
                cidrList = payload.cidrList
                tokenBoundCidrs = payload.tokenBoundCidrs
                numUses = payload.numUses
                ttl = payload.ttl
            }
            .build() shouldBe payload
    }

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
