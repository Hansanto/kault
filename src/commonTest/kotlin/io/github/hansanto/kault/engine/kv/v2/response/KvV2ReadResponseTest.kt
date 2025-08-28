package io.github.hansanto.kault.engine.kv.v2.response

import io.github.hansanto.kault.common.Metadata
import io.github.hansanto.kault.util.ComplexSerializableClass
import io.github.hansanto.kault.util.SimpleSerializableClass
import io.github.hansanto.kault.util.randomBoolean
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject
import kotlin.collections.plus
import kotlin.test.Test
import kotlin.time.Instant

class KvV2ReadResponseTest {

    @Test
    fun `should check is deleted return true when data is null and metadata is not destroyed`() = runTest {
        val response = KvV2ReadResponse(null, createResponse(false))
        response.isDeleted() shouldBe true
    }

    @Test
    fun `should check is deleted return false when data is not null`() = runTest {
        val response = KvV2ReadResponse(
            JsonObject(emptyMap()),
            createResponse(randomBoolean())
        )
        response.isDeleted() shouldBe false
    }

    @Test
    fun `should check is deleted return false when metadata is destroyed`() = runTest {
        val response = KvV2ReadResponse(null, createResponse(true))
        response.isDeleted() shouldBe false
    }

    @Test
    fun `should check is destroyed return true when metadata is destroyed`() = runTest {
        val response = KvV2ReadResponse(null, createResponse(true))
        response.isDestroyed() shouldBe true
    }

    @Test
    fun `should check is destroyed return false when metadata is not destroyed`() = runTest {
        val response = KvV2ReadResponse(
            JsonObject(emptyMap()),
            createResponse(false)
        )
        response.isDestroyed() shouldBe false
    }

    @Test
    fun `should decode value`() = runTest {
        val value = createSerializableObject()
        val customMetadata = Json.encodeToJsonElement(value).jsonObject
        val response = createResponse(customMetadata)
        response.data<ComplexSerializableClass>() shouldBe value
    }

    @Test
    fun `should return null when value is null`() = runTest {
        val response = createResponse(null)
        response.data<ComplexSerializableClass>() shouldBe null
    }

    @Test
    fun `should ignore non-registered fields`() = runTest {
        val value = createSerializableObject()
        val dataMap = Json.encodeToJsonElement(value).jsonObject + ("extra" to Json.encodeToJsonElement("extra"))
        val data = JsonObject(dataMap)
        val response = createResponse(data)
        response.data<ComplexSerializableClass>() shouldBe value
    }

    @Test
    fun `should throw exception if missing required fields`() = runTest {
        val data = JsonObject(mapOf("a" to Json.encodeToJsonElement("value")))
        val response = createResponse(data)
        shouldThrow<IllegalArgumentException> {
            response.data<ComplexSerializableClass>()
        }
    }
}

private fun createResponse(data: JsonObject?) = KvV2ReadResponse(
    data = data,
    metadata = createResponse(randomBoolean())
)

private fun createResponse(destroyed: Boolean) = Metadata(
    createdTime = Instant.DISTANT_PAST,
    deletionTime = Instant.DISTANT_PAST,
    customMetadata = null,
    destroyed = destroyed,
    version = 1
)

private fun createSerializableObject() = ComplexSerializableClass("value", 1, SimpleSerializableClass("value2", 2))
