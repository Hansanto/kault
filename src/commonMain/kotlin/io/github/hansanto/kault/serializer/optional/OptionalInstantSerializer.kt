package io.github.hansanto.kault.serializer.optional

import kotlin.time.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.nullable
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

/**
 * Serializer for optional [Instant] value.
 * When a string is empty or null, it will be deserialized as null, otherwise it will be deserialized as a [Instant].
 * When a [Instant] is null, it will be serialized as null, otherwise, it will be serialized as a string.
 */
public object OptionalInstantSerializer : KSerializer<Instant?> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("OptionalInstant", PrimitiveKind.STRING).nullable

    override fun deserialize(decoder: Decoder): Instant? {
        val value = decoder.decodeNullableSerializableValue(String.serializer())
        return if (value.isNullOrEmpty()) {
            null
        } else {
            Instant.parse(value)
        }
    }

    override fun serialize(encoder: Encoder, value: Instant?) {
        if (value == null) {
            encoder.encodeNull()
        } else {
            Instant.serializer().serialize(encoder, value)
        }
    }
}
