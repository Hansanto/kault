package io.github.hansanto.kault.engine.kv.v2.response

import io.github.hansanto.kault.common.Metadata
import io.github.hansanto.kault.compose.JsonDecoderComposer
import io.github.hansanto.kault.util.ComplexSerializableClass
import io.github.hansanto.kault.util.randomBoolean
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject

class KvV2ReadResponseTest : FunSpec({

    test("is deleted should return true when data is null and metadata is not destroyed") {
        val response = KvV2ReadResponse(null, createResponse(false))
        response.isDeleted() shouldBe true
    }

    test("is deleted should return false when data is not null") {
        val response = KvV2ReadResponse(
            JsonObject(emptyMap()),
            createResponse(randomBoolean())
        )
        response.isDeleted() shouldBe false
    }

    test("is deleted should return false when metadata is destroyed") {
        val response = KvV2ReadResponse(null, createResponse(true))
        response.isDeleted() shouldBe false
    }

    test("is destroyed should return true when metadata is destroyed") {
        val response = KvV2ReadResponse(null, createResponse(true))
        response.isDestroyed() shouldBe true
    }

    test("is destroyed should return false when metadata is not destroyed") {
        val response = KvV2ReadResponse(
            JsonObject(emptyMap()),
            createResponse(false)
        )
        response.isDestroyed() shouldBe false
    }

    JsonDecoderComposer.composeSerialFieldTest(this) {
        val response = createResponse(it)
        response.data<ComplexSerializableClass>()
    }
})

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
