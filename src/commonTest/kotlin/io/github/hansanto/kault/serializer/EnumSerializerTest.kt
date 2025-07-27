package io.github.hansanto.kault.serializer

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

object TestEnumSerializerWithValue : EnumSerializer<TestEnum>("TestEnumValue", TestEnum.entries, {
    it.value.toString()
})

object TestEnumSerializerWithOrdinal : EnumSerializer<TestEnum>("TestEnumOrdinal", TestEnum.entries, {
    it.ordinal.toString()
})

object TestEnumSerializerWithName : EnumSerializer<TestEnum>("TestEnumName", TestEnum.entries, { it.name.lowercase() })

enum class TestEnum(val value: Int) {
    ONE(1),
    TWO(2),
    THREE(3)
}

class EnumSerializerTest :
    ShouldSpec({

        should("serialize with given way") {
            assertEnumSerialized(TestEnumSerializerWithValue, TestEnum.ONE, "1")
            assertEnumSerialized(TestEnumSerializerWithValue, TestEnum.TWO, "2")
            assertEnumSerialized(TestEnumSerializerWithValue, TestEnum.THREE, "3")

            assertEnumSerialized(TestEnumSerializerWithOrdinal, TestEnum.ONE, "0")
            assertEnumSerialized(TestEnumSerializerWithOrdinal, TestEnum.TWO, "1")
            assertEnumSerialized(TestEnumSerializerWithOrdinal, TestEnum.THREE, "2")

            assertEnumSerialized(TestEnumSerializerWithName, TestEnum.ONE, "one")
            assertEnumSerialized(TestEnumSerializerWithName, TestEnum.TWO, "two")
            assertEnumSerialized(TestEnumSerializerWithName, TestEnum.THREE, "three")
        }

        should("deserialize with given way") {
            assertEnumDeserialized(TestEnumSerializerWithValue, "1", TestEnum.ONE)
            assertEnumDeserialized(TestEnumSerializerWithValue, "2", TestEnum.TWO)
            assertEnumDeserialized(TestEnumSerializerWithValue, "3", TestEnum.THREE)

            assertEnumDeserialized(TestEnumSerializerWithOrdinal, "0", TestEnum.ONE)
            assertEnumDeserialized(TestEnumSerializerWithOrdinal, "1", TestEnum.TWO)
            assertEnumDeserialized(TestEnumSerializerWithOrdinal, "2", TestEnum.THREE)

            assertEnumDeserialized(TestEnumSerializerWithName, "one", TestEnum.ONE)
            assertEnumDeserialized(TestEnumSerializerWithName, "two", TestEnum.TWO)
            assertEnumDeserialized(TestEnumSerializerWithName, "three", TestEnum.THREE)
        }

        should("deserialize throws error if value not recognized") {
            assertEnumDeserializedError(TestEnumSerializerWithValue, "4")
            assertEnumDeserializedError(TestEnumSerializerWithOrdinal, "3")
            assertEnumDeserializedError(TestEnumSerializerWithName, "four")
        }
    })

private fun assertEnumSerialized(serializer: KSerializer<TestEnum>, enum: TestEnum, expected: String) {
    val json = """
        "$expected"
    """.trimIndent()
    Json.encodeToString(serializer, enum) shouldEqualJson json
}

private fun assertEnumDeserialized(serializer: KSerializer<TestEnum>, value: String, expected: TestEnum) {
    val json = """
        "$value"
    """.trimIndent()
    Json.decodeFromString(serializer, json) shouldBe expected
}

private fun assertEnumDeserializedError(serializer: KSerializer<TestEnum>, value: String) {
    val json = """
        "$value"
    """.trimIndent()

    shouldThrow<SerializationException> {
        Json.decodeFromString(serializer, json)
    }
}
