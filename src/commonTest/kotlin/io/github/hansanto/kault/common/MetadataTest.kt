package io.github.hansanto.kault.common

import io.github.hansanto.kault.tester.JsonDecoderTester
import io.github.hansanto.kault.util.ComplexSerializableClass
import io.kotest.core.spec.style.FunSpec
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject

class MetadataTest : FunSpec({

    JsonDecoderTester.testDecode(this) {
        val response = createMetadata(it)
        response.customMetadata<ComplexSerializableClass>()
    }
})

private fun createMetadata(customMetadata: JsonObject?) = Metadata(
    createdTime = Instant.DISTANT_PAST,
    customMetadata = customMetadata,
    deletionTime = Instant.DISTANT_PAST,
    destroyed = false,
    version = 1
)
