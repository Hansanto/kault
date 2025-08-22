package io.github.hansanto.kault.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.enums.EnumEntries

/**
 * EnumSerializer is a serializer used to serialize and deserialize enum values.
 *
 * @param T The type of the enum class.
 * @property values The list of enum values to be serialized or deserialized.
 * @property enumToValue The function used to transform an enum value to a string.
 * @constructor Creates an instance of EnumSerializer with the given serial name and enum values.
 */
public abstract class EnumSerializer<T : Enum<T>>(
    serialName: String,
    private val values: EnumEntries<T>,
    private val enumToValue: (T) -> String
) : KSerializer<T> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(serialName, PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: T) {
        encoder.encodeString(enumToValue(value))
    }

    override fun deserialize(decoder: Decoder): T = findEnumValue(decoder.decodeString())

    /**
     * Finds the matching enum value for a given decoded string.
     *
     * @param decoded The decoded string used to search for the matching enum value.
     * @return The matching enum value.
     * @throws SerializationException if no matching enum value is found.
     */
    private fun findEnumValue(decoded: String): T = values.firstOrNull { enumToValue(it) == decoded }
        ?: throw SerializationException(
            "Invalid enum value: $decoded. Valid values are: ${
                values.joinToString { enumToValue(it) }
            }"
        )
}
