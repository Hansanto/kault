package io.github.hansanto.kault.auth.common.common

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

class TokenTypeSerializerTest :
    ShouldSpec({

        should("encode with name lowercase property") {
            TokenType.entries.forEach { value ->
                val serialized = Json.encodeToJsonElement(TokenTypeSerializer, value)
                serialized.jsonPrimitive.content shouldBe value.value
            }
        }

        should("decode with name lowercase property") {
            TokenType.entries.forEach { value ->
                val serialized = Json.encodeToJsonElement(TokenTypeSerializer, value)
                Json.decodeFromJsonElement(TokenTypeSerializer, serialized) shouldBe value
            }
        }
    })
