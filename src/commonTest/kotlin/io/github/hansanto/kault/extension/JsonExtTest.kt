package io.github.hansanto.kault.extension

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.serializer

class JsonExtTest :
    ShouldSpec({

        should("transform an empty map to a empty json string") {
            val map = emptyMap<String, String>()
            val expected = "{}"
            val actual = map.toJsonString(String.serializer(), String.serializer())
            actual shouldBe expected
        }

        should("transform a map with one entry to a json string") {
            val map = mapOf("key1" to "value1")
            val expected = "{\"key1\":\"value1\"}"
            val actual = map.toJsonString(String.serializer(), String.serializer())
            actual shouldBe expected
        }

        should("transform a map with two entries to a json string") {
            val map = mapOf("key1" to "value1", "key2" to "value2")
            val expected = "{\"key1\":\"value1\",\"key2\":\"value2\"}"
            val actual = map.toJsonString(String.serializer(), String.serializer())
            actual shouldBe expected
        }

        should("transform a map with two entries of different types to a json string") {
            val map = mapOf("key1" to 1, "key2" to 2)
            val expected = "{\"key1\":1,\"key2\":2}"
            val actual = map.toJsonString(String.serializer(), Int.serializer())
            actual shouldBe expected
        }
    })
