package io.github.hansanto.kault.extension

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.builtins.serializer
import kotlin.test.Test

class JsonExtTest {

    @Test
    fun `should transform an empty map to a empty json string`() = runTest {
        val map = emptyMap<String, String>()
        val expected = "{}"
        val actual = map.toJsonString(String.serializer(), String.serializer())
        actual shouldBe expected
    }

    @Test
    fun `should transform a map with one entry to a json string`() = runTest {
        val map = mapOf("key1" to "value1")
        val expected = "{\"key1\":\"value1\"}"
        val actual = map.toJsonString(String.serializer(), String.serializer())
        actual shouldBe expected
    }

    @Test
    fun `should transform a map with two entries to a json string`() = runTest {
        val map = mapOf("key1" to "value1", "key2" to "value2")
        val expected = "{\"key1\":\"value1\",\"key2\":\"value2\"}"
        val actual = map.toJsonString(String.serializer(), String.serializer())
        actual shouldBe expected
    }

    @Test
    fun `should transform a map with two entries of different types to a json string`() = runTest {
        val map = mapOf("key1" to 1, "key2" to 2)
        val expected = "{\"key1\":1,\"key2\":2}"
        val actual = map.toJsonString(String.serializer(), Int.serializer())
        actual shouldBe expected
    }
}
