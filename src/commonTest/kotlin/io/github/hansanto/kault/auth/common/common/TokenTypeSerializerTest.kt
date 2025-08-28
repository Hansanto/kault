package io.github.hansanto.kault.auth.common.common

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test

class TokenTypeSerializerTest {

    @Test
    fun `should encode with name lowercase property`() = runTest {
        TokenType.entries.forEach { value ->
            val serialized = Json.encodeToJsonElement(TypeSerializer, value)
            serialized.jsonPrimitive.content shouldBe value.value
        }
    }

    @Test
    fun `should decode with name lowercase property`() = runTest {
        TokenType.entries.forEach { value ->
            val serialized = Json.encodeToJsonElement(TypeSerializer, value)
            Json.decodeFromJsonElement(TypeSerializer, serialized) shouldBe value
        }
    }
}
