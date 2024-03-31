package io.github.hansanto.kault.engine.kv.v2.response

import io.github.hansanto.kault.compose.JsonDecoderComposer
import io.github.hansanto.kault.util.ComplexSerializableClass
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject

class KvV2WriteResponseTest : ShouldSpec({

    JsonDecoderComposer.composeSerialFieldTest(this) {
        val response = createResponse(it)
        response.customMetadata<ComplexSerializableClass>()
    }
})

private fun createResponse(customMetadata: JsonObject?) = KvV2WriteResponse(
    createdTime = Instant.DISTANT_PAST,
    customMetadata = customMetadata,
    deletionTime = Instant.DISTANT_PAST,
    destroyed = false,
    version = 1
)
