package com.github.hansanto.kault.extension

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.serializer

class JsonExtTest : FunSpec({

    test("map to json string with empty map") {
        val map = emptyMap<String, String>()
        val expected = "{}"
        val actual = map.toJsonString(String.serializer(), String.serializer())
        actual shouldBe expected
    }

    test("map to json string with map with one entry") {
        val map = mapOf("key1" to "value1")
        val expected = "{\"key1\":\"value1\"}"
        val actual = map.toJsonString(String.serializer(), String.serializer())
        actual shouldBe expected
    }

    test("map to json string with map with two entries") {
        val map = mapOf("key1" to "value1", "key2" to "value2")
        val expected = "{\"key1\":\"value1\",\"key2\":\"value2\"}"
        val actual = map.toJsonString(String.serializer(), String.serializer())
        actual shouldBe expected
    }

    test("map to json string with map with two entries with int") {
        val map = mapOf("key1" to 1, "key2" to 2)
        val expected = "{\"key1\":1,\"key2\":2}"
        val actual = map.toJsonString(String.serializer(), Int.serializer())
        actual shouldBe expected
    }
})
