package io.github.hansanto.kault.serializer.optional

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class WrapperOptionalInstantValue(
    @Serializable(with = OptionalInstantSerializer::class)
    val value: Instant?
)

class OptionalInstantSerializerTest : FunSpec({

    test("serialize with null value") {
        assertSerialized(null, null)
    }

    test("serialize with value") {
        assertSerialized(Instant.parse("2030-12-20T14:42:52Z"), "2030-12-20T14:42:52Z")
        assertSerialized(Instant.fromEpochMilliseconds(0), "1970-01-01T00:00:00Z")
    }

    test("deserialize with null value") {
        assertDeserialized(null, null)
    }

    test("deserialize with empty string") {
        assertDeserialized("", null)
    }

    test("deserialize with wrong string") {
        assertInvalidFormat("wrong")
    }

    test("deserialize with valid") {
        assertDeserialized("2030-12-20T14:42:52Z", Instant.parse("2030-12-20T14:42:52Z"))
        assertDeserialized("1970-01-01T00:00:00Z", Instant.fromEpochMilliseconds(0))
    }
})

private fun assertSerialized(instant: Instant?, expected: String?) {
    val wrapper = WrapperOptionalInstantValue(instant)
    val json = createWrapperJson(expected)
    Json.encodeToString(wrapper) shouldEqualJson json
}

private fun assertDeserialized(instant: String?, expected: Instant?) {
    val json = createWrapperJson(instant)
    val wrapper = Json.decodeFromString(WrapperOptionalInstantValue.serializer(), json)
    wrapper.value shouldBe expected
}

private fun createWrapperJson(instant: String?) = if (instant == null) {
    """
       {"value":null}
    """.trimIndent()
} else {
    """
       {"value":"$instant"}
    """.trimIndent()
}

private fun assertInvalidFormat(value: String) {
    val json = """
        "$value"
    """.trimIndent()
    shouldThrow<Exception> {
        Json.decodeFromString(OptionalInstantSerializer, json)
    }
}
