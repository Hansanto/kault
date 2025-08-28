package io.github.hansanto.kault.common

import io.github.hansanto.kault.util.ComplexSerializableClass
import io.github.hansanto.kault.util.SimpleSerializableClass
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.test.Test
import kotlin.time.Instant

class MetadataTest {

    @Test
    fun `should decode value`() = runTest {
        val value = createSerializableObject()
        val customMetadata = Json.encodeToJsonElement(value).jsonObject
        createMetadata(customMetadata).customMetadata<ComplexSerializableClass>() shouldBe value
    }

    @Test
    fun `should return null when value is null`() = runTest {
        createMetadata(null).customMetadata<ComplexSerializableClass>() shouldBe null
    }

    @Test
    fun `should ignore non-registered fields`() = runTest {
        val value = createSerializableObject()
        val dataMap = Json.encodeToJsonElement(value).jsonObject + ("extra" to Json.encodeToJsonElement("extra"))
        val data = JsonObject(dataMap)
        createMetadata(data).customMetadata<ComplexSerializableClass>() shouldBe value
    }

    @Test
    fun `should throw exception if missing required fields`() = runTest {
        val data = JsonObject(mapOf("a" to Json.encodeToJsonElement("value")))
        val metadata = createMetadata(data)
        shouldThrow<IllegalArgumentException> {
            metadata.customMetadata<ComplexSerializableClass>()
        }
    }
}

private fun createMetadata(customMetadata: JsonObject?) = Metadata(
    createdTime = Instant.DISTANT_PAST,
    customMetadata = customMetadata,
    deletionTime = Instant.DISTANT_PAST,
    destroyed = false,
    version = 1
)

private fun createSerializableObject() = ComplexSerializableClass("value", 1, SimpleSerializableClass("value2", 2))
