package io.github.hansanto.kault.system.auth.common

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

class ListingVisibilitySerializerTest : ShouldSpec({

    should("encode with name lowercase property") {
        ListingVisibility.entries.forEach { visibility ->
            val serialized = Json.encodeToJsonElement(ListingVisibilitySerializer, visibility)
            serialized.jsonPrimitive.content shouldBe visibility.name.lowercase()
        }
    }

    should("decode with name lowercase property") {
        ListingVisibility.entries.forEach { visibility ->
            val serialized = Json.encodeToJsonElement(ListingVisibilitySerializer, visibility)
            Json.decodeFromJsonElement(ListingVisibilitySerializer, serialized) shouldBe visibility
        }
    }
})
