package io.github.hansanto.kault.engine.kv.v2.response

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

class KvV2WriteResponseTest {

    @Test
    fun `should decode value`() = runTest {
        val value = createSerializableObject()
        val customMetadata = Json.encodeToJsonElement(value).jsonObject
        val response = createResponse(customMetadata)
        response.customMetadata<ComplexSerializableClass>() shouldBe value
    }

    @Test
    fun `should return null when value is null`() = runTest {
        val response = createResponse(null)
        response.customMetadata<ComplexSerializableClass>() shouldBe null
    }

    @Test
    fun `should ignore non-registered fields`() = runTest {
        val value = createSerializableObject()
        val dataMap = Json.encodeToJsonElement(value).jsonObject + ("extra" to Json.encodeToJsonElement("extra"))
        val data = JsonObject(dataMap)
        val response = createResponse(data)
        response.customMetadata<ComplexSerializableClass>() shouldBe value
    }

    @Test
    fun `should throw exception if missing required fields`() = runTest {
        val data = JsonObject(mapOf("a" to Json.encodeToJsonElement("value")))
        val response = createResponse(data)
        shouldThrow<IllegalArgumentException> {
            response.customMetadata<ComplexSerializableClass>()
        }
    }
}

private fun createResponse(customMetadata: JsonObject?) = KvV2WriteResponse(
    createdTime = Instant.DISTANT_PAST,
    customMetadata = customMetadata,
    deletionTime = Instant.DISTANT_PAST,
    destroyed = false,
    version = 1
)

private fun createSerializableObject() = ComplexSerializableClass("value", 1, SimpleSerializableClass("value2", 2))
