package com.github.hansanto.kault.system.auth.common

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable(with = ListingVisibilitySerializer::class)
public enum class ListingVisibility(public val value: String) {

    /**
     * Mount is not visible.
     */
    HIDDEN("hidden"),

    /**
     * Mount is marked as internal-only.
     */
    UNAUTH("unauth");
}

/**
 * Serializer for [ListingVisibility] based on the [value][ListingVisibility.value] property.
 */
public object ListingVisibilitySerializer : KSerializer<ListingVisibility> {

    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("listingVisibility", PrimitiveKind.STRING)

    override fun serialize(encoder: Encoder, value: ListingVisibility) {
        encoder.encodeString(value.value)
    }

    override fun deserialize(decoder: Decoder): ListingVisibility {
        val value = decoder.decodeString()
        return ListingVisibility.entries.find {
            it.value == value
        } ?: throw SerializationException(
            "No matching ListingVisibility for value [$value], expected one of ${
            ListingVisibility.entries.map {
                it.value
            }
            }"
        )
    }
}
