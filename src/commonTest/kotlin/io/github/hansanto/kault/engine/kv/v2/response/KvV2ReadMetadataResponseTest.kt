package io.github.hansanto.kault.engine.kv.v2.response

import io.github.hansanto.kault.tester.JsonDecoderTester
import io.github.hansanto.kault.util.ComplexSerializableClass
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomLong
import io.kotest.core.spec.style.FunSpec
import kotlinx.datetime.Instant
import kotlinx.serialization.json.JsonObject
import kotlin.time.Duration

class KvV2ReadMetadataResponseTest : FunSpec({

    JsonDecoderTester.testDecode(this) {
        val response = createResponse(it)
        response.customMetadata<ComplexSerializableClass>()
    }
})

private fun createResponse(customMetadata: JsonObject?) = KvV2ReadMetadataResponse(
    casRequired = randomBoolean(),
    createdTime = Instant.DISTANT_PAST,
    currentVersion = randomLong(),
    customMetadata = customMetadata,
    deleteVersionAfter = Duration.ZERO,
    maxVersions = randomLong(),
    oldestVersion = randomLong(),
    updatedTime = Instant.DISTANT_FUTURE,
    versions = emptyMap()
)
