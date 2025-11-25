package io.github.hansanto.kault.extension

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.JsonPrimitive

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

        should("transform null value to JsonPrimitive") {
            null.toJsonPrimitive() shouldBe JsonPrimitive(null)
        }

        should("transform string value to JsonPrimitive") {
            (0..10).forEach {
                it.toString().toJsonPrimitive() shouldBe JsonPrimitive(it.toString())
            }
        }

        should("transform number value to JsonPrimitive") {
            (-10..10).forEach {
                it.toJsonPrimitive() shouldBe JsonPrimitive(it)
            }
        }

        should("transform boolean value to JsonPrimitive") {
            true.toJsonPrimitive() shouldBe JsonPrimitive(true)
            false.toJsonPrimitive() shouldBe JsonPrimitive(false)
        }

        should("transform double value to JsonPrimitive") {
            3.14.toJsonPrimitive() shouldBe JsonPrimitive(3.14)
            (-2.71).toJsonPrimitive() shouldBe JsonPrimitive(-2.71)
            (0.0).toJsonPrimitive() shouldBe JsonPrimitive(0.0)
        }

        should("transform list value to JsonPrimitive") {
            listOf(1, 2, 3).toJsonPrimitive() shouldBe JsonPrimitive("[1, 2, 3]")
            List(10) { 'a' + it }.toJsonPrimitive() shouldBe JsonPrimitive("[a, b, c, d, e, f, g, h, i, j]")
        }

        should("not transform a JsonPrimitive value") {
            val nullOriginal = JsonPrimitive(null)
            nullOriginal.toJsonPrimitive() shouldBe nullOriginal

            (-5..5).forEach {
                val stringOriginal = JsonPrimitive("test-$it")
                stringOriginal.toJsonPrimitive() shouldBe stringOriginal

                val numberOriginal = JsonPrimitive(it)
                numberOriginal.toJsonPrimitive() shouldBe numberOriginal
            }

            val trueOriginal = JsonPrimitive(true)
            trueOriginal.toJsonPrimitive() shouldBe trueOriginal

            val falseOriginal = JsonPrimitive(false)
            falseOriginal.toJsonPrimitive() shouldBe falseOriginal
        }

        should("transform map value to JsonPrimitive") {
            mapOf("key" to "value").toJsonPrimitive() shouldBe JsonPrimitive("{key=value}")
            mapOf(1 to 2, 3 to 4).toJsonPrimitive() shouldBe JsonPrimitive("{1=2, 3=4}")
            mapOf(
                "key1" to 1,
                "key2" to listOf(1, 2),
                "key3" to mapOf("nestedKey" to "nestedValue")
            ).toJsonPrimitive() shouldBe JsonPrimitive("{key1=1, key2=[1, 2], key3={nestedKey=nestedValue}}")
        }

        should("transform custom object to JsonPrimitive") {
            data class CustomObject(val id: Int, val name: String)
            CustomObject(1, "Test").toJsonPrimitive() shouldBe JsonPrimitive("CustomObject(id=1, name=Test)")
            CustomObject(42, "Example").toJsonPrimitive() shouldBe JsonPrimitive("CustomObject(id=42, name=Example)")
        }

        should("transform an empty map to an empty JsonPrimitive map") {
            emptyMap<String, Any?>() shouldBe emptyMap<String, JsonPrimitive>()
        }

        should("transform a map with various types to a JsonPrimitive map") {
            data class CustomObject(val id: Int, val name: String)

            val map = mapOf(
                "nullKey" to null,
                "stringKey" to "stringValue",
                "intKey" to 49.1475,
                "boolKey" to true,
                "listKey" to listOf(1, 'a'),
                "mapKey" to mapOf("innerKey" to "innerValue"),
                "customObjectKey" to CustomObject(-1, "0451")
            )
            val expected = mapOf(
                "nullKey" to JsonPrimitive(null),
                "stringKey" to JsonPrimitive("stringValue"),
                "intKey" to JsonPrimitive(49.1475),
                "boolKey" to JsonPrimitive(true),
                "listKey" to JsonPrimitive("[1, a]"),
                "mapKey" to JsonPrimitive("{innerKey=innerValue}"),
                "customObjectKey" to JsonPrimitive("CustomObject(id=-1, name=0451)")
            )
            val actual = map.toJsonPrimitiveMap()
            actual shouldBe expected
        }
    })
