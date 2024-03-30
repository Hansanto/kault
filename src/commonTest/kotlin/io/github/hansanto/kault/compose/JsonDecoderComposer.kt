package io.github.hansanto.kault.compose

import io.github.hansanto.kault.util.ComplexSerializableClass
import io.github.hansanto.kault.util.SimpleSerializableClass
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

object JsonDecoderComposer {

    fun composeSerialFieldTest(
        scope: ShouldSpec,
        createAndRetrieve: (JsonObject?) -> ComplexSerializableClass?
    ) {
        scope.should("decode value") {
            val value = createSerializableObject()
            val customMetadata = Json.encodeToJsonElement(value).jsonObject
            createAndRetrieve(customMetadata) shouldBe value
        }

        scope.should("return null when value is null") {
            createAndRetrieve(null) shouldBe null
        }

        scope.should("ignore non registered fields") {
            val value = createSerializableObject()
            val dataMap = Json.encodeToJsonElement(value).jsonObject + ("extra" to Json.encodeToJsonElement("extra"))
            val data = JsonObject(dataMap)
            createAndRetrieve(data) shouldBe value
        }

        scope.should("throw exception if missing required fields") {
            val data = JsonObject(mapOf("a" to Json.encodeToJsonElement("value")))
            shouldThrow<IllegalArgumentException> {
                createAndRetrieve(data)
            }
        }
    }

    private fun createSerializableObject() = ComplexSerializableClass("value", 1, SimpleSerializableClass("value2", 2))
}
