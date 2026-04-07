package io.github.hansanto.kault.auth.kubernetes.common

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonPrimitive

class AuthKubernetesAliasNameSourceTypeSerializerTest :
    ShouldSpec({

        should("encode with name lowercase property") {
            AuthKubernetesAliasNameSourceType.entries.forEach { value ->
                val serialized = Json.encodeToJsonElement(AuthKubernetesAliasNameSourceTypeSerializer, value)
                serialized.jsonPrimitive.content shouldBe value.value
            }
        }

        should("decode with name lowercase property") {
            AuthKubernetesAliasNameSourceType.entries.forEach { value ->
                val serialized = Json.encodeToJsonElement(AuthKubernetesAliasNameSourceTypeSerializer, value)
                Json.decodeFromJsonElement(AuthKubernetesAliasNameSourceTypeSerializer, serialized) shouldBe value
            }
        }
    })
