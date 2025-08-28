package io.github.hansanto.kault.auth.kubernetes.common

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive
import kotlin.test.Test

class KubernetesAliasNameSourceTypeSerializerTest {

    @Test
    fun `should encode with name lowercase property`() = runTest {
        KubernetesAliasNameSourceType.entries.forEach { value ->
            val serialized = Json.encodeToJsonElement(KubernetesAliasNameSourceTypeSerializer, value)
            serialized.jsonPrimitive.content shouldBe value.value
        }
    }

    @Test
    fun `should decode with name lowercase property`() = runTest {
        KubernetesAliasNameSourceType.entries.forEach { value ->
            val serialized = Json.encodeToJsonElement(KubernetesAliasNameSourceTypeSerializer, value)
            Json.decodeFromJsonElement(KubernetesAliasNameSourceTypeSerializer, serialized) shouldBe value
        }
    }
}
