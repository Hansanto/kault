package io.github.hansanto.kault.auth.approle.common

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

class AppRoleTokenTypeSerializerTest : FunSpec({

    test("should encode with name lowercase property") {
        AppRoleTokenType.entries.forEach { value ->
            val serialized = Json.encodeToJsonElement(AppRoleTokenTypeSerializer, value)
            serialized.jsonPrimitive.content shouldBe value.value
        }
    }

    test("should decode with name lowercase property") {
        AppRoleTokenType.entries.forEach { value ->
            val serialized = Json.encodeToJsonElement(AppRoleTokenTypeSerializer, value)
            Json.decodeFromJsonElement(AppRoleTokenTypeSerializer, serialized) shouldBe value
        }
    }
})
