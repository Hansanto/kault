package io.github.hansanto.kault.common

import io.github.hansanto.kault.compose.JsonDecoderComposer
import io.github.hansanto.kault.util.ComplexSerializableClass
import io.kotest.core.spec.style.ShouldSpec
import kotlinx.serialization.json.JsonObject
import kotlin.time.Instant

class MetadataTest :
    ShouldSpec({

        JsonDecoderComposer.composeSerialFieldTest(this) {
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
