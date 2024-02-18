package io.github.hansanto.kault.test

import io.github.hansanto.kault.util.ComplexSerializableClass
import io.github.hansanto.kault.util.SimpleSerializableClass
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

object JsonObjectDecoderTester {

    fun testDecode(
        scope: FunSpec,
        createAndRetrieve: (JsonObject?) -> ComplexSerializableClass?
    ) {
        scope.test("should decode custom metadata") {
            val value = createSerializableObject()
            val customMetadata = Json.encodeToJsonElement(value).jsonObject
            createAndRetrieve(customMetadata) shouldBe value
        }

        scope.test("should return null when data is null") {
            createAndRetrieve(null) shouldBe null
        }

        scope.test("should ignore non registered fields") {
            val value = createSerializableObject()
            val dataMap = Json.encodeToJsonElement(value).jsonObject + ("extra" to Json.encodeToJsonElement("extra"))
            val data = JsonObject(dataMap)
            createAndRetrieve(data) shouldBe value
        }

        scope.test("should throw exception if missing required fields") {
            val data = JsonObject(mapOf("a" to Json.encodeToJsonElement("value")))
            shouldThrow<IllegalArgumentException> {
                createAndRetrieve(data)
            }
        }
    }

    private fun createSerializableObject() = ComplexSerializableClass("value", 1, SimpleSerializableClass("value2", 2))
}
