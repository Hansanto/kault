package io.github.hansanto.kault.system.auth.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

class ListingVisibilitySerializerTest : FunSpec({

    test("should encode with name lowercase property") {
        ListingVisibility.entries.forEach { visibility ->
            val serialized = Json.encodeToJsonElement(ListingVisibilitySerializer, visibility)
            serialized.jsonPrimitive.content shouldBe visibility.name.lowercase()
        }
    }

    test("should decode with name lowercase property") {
        ListingVisibility.entries.forEach { visibility ->
            val serialized = Json.encodeToJsonElement(ListingVisibilitySerializer, visibility)
            Json.decodeFromJsonElement(ListingVisibilitySerializer, serialized) shouldBe visibility
        }
    }
})
