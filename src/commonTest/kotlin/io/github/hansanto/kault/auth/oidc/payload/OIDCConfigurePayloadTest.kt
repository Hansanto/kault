package io.github.hansanto.kault.auth.oidc.payload

import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomLong
import io.github.hansanto.kault.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

class OIDCConfigurePayloadTest :
    ShouldSpec({

        should("set empty map for providerConfig from builder") {
            val builder = OIDCConfigurePayload
                .Builder().apply {
                    providerConfig = mapOf()
                }

            val payload = builder.build()
            payload.providerConfig shouldBe emptyMap()
        }

        should("set map with primitive values for providerConfig from builder") {
            val randomString = randomString()
            val randomLong = randomLong()
            val randomBoolean = randomBoolean()

            val builder = OIDCConfigurePayload
                .Builder().apply {
                    providerConfig = mapOf(
                        "stringKey" to randomString,
                        "longKey" to randomLong,
                        "booleanKey" to randomBoolean,
                        "nullKey" to null
                    )
                }

            val payload = builder.build()
            payload.providerConfig shouldBe mapOf(
                "stringKey" to JsonPrimitive(randomString),
                "longKey" to JsonPrimitive(randomLong),
                "booleanKey" to JsonPrimitive(randomBoolean),
                "nullKey" to JsonNull
            )
        }

        should("set map with non-primitive value for providerConfig from builder") {
            val builder = OIDCConfigurePayload
                .Builder().apply {
                    providerConfig = mapOf(
                        "primitive" to "stringValue",
                        "non-primitive" to listOf(1, 2, 3)
                    )
                }

            val payload = builder.build()
            payload.providerConfig shouldBe mapOf(
                "primitive" to JsonPrimitive("stringValue"),
                "non-primitive" to JsonPrimitive("[1, 2, 3]")
            )
        }
    })
