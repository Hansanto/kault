package io.github.hansanto.kault.system.auth.common

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test

class ListingVisibilitySerializerTest {

    @Test
    fun `should encode with name lowercase property`() = runTest {
        ListingVisibility.entries.forEach { visibility ->
            val serialized = Json.encodeToJsonElement(ListingVisibilitySerializer, visibility)
            serialized.jsonPrimitive.content shouldBe visibility.name.lowercase()
        }
    }

    @Test
    fun `should decode with name lowercase property`() = runTest {
        ListingVisibility.entries.forEach { visibility ->
            val serialized = Json.encodeToJsonElement(ListingVisibilitySerializer, visibility)
            Json.decodeFromJsonElement(ListingVisibilitySerializer, serialized) shouldBe visibility
        }
    }
}
