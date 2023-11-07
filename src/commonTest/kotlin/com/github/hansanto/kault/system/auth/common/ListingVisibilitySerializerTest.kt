package com.github.hansanto.kault.system.auth.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

class ListingVisibilitySerializerTest : FunSpec({

    test("should encode with value property") {
        ListingVisibility.entries.forEach { visibility ->
            val serialized = Json.encodeToJsonElement(ListingVisibilitySerializer, visibility)
            serialized.jsonPrimitive.content shouldBe visibility.value
        }
    }

    test("should decode with value property") {
        ListingVisibility.entries.forEach { visibility ->
            val serialized = Json.encodeToJsonElement(ListingVisibilitySerializer, visibility)
            Json.decodeFromJsonElement(ListingVisibilitySerializer, serialized) shouldBe visibility
        }
    }
})
