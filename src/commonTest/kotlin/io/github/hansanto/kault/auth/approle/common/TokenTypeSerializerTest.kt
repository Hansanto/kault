package io.github.hansanto.kault.auth.approle.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

class TokenTypeSerializerTest : FunSpec({

    test("should encode with name lowercase property") {
        TokenType.entries.forEach { value ->
            val serialized = Json.encodeToJsonElement(TokenTypeSerializer, value)
            serialized.jsonPrimitive.content shouldBe value.value
        }
    }

    test("should decode with name lowercase property") {
        TokenType.entries.forEach { value ->
            val serialized = Json.encodeToJsonElement(TokenTypeSerializer, value)
            Json.decodeFromJsonElement(TokenTypeSerializer, serialized) shouldBe value
        }
    }
})
