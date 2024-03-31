package io.github.hansanto.kault.auth.kubernetes.common

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

class KubernetesAliasNameSourceTypeSerializerTest : ShouldSpec({

    should("encode with name lowercase property") {
        KubernetesAliasNameSourceType.entries.forEach { value ->
            val serialized = Json.encodeToJsonElement(KubernetesAliasNameSourceTypeSerializer, value)
            serialized.jsonPrimitive.content shouldBe value.value
        }
    }

    should("decode with name lowercase property") {
        KubernetesAliasNameSourceType.entries.forEach { value ->
            val serialized = Json.encodeToJsonElement(KubernetesAliasNameSourceTypeSerializer, value)
            Json.decodeFromJsonElement(KubernetesAliasNameSourceTypeSerializer, serialized) shouldBe value
        }
    }
})
