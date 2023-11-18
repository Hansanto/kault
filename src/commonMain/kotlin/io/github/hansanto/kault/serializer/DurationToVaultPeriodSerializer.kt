package io.github.hansanto.kault.serializer

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

/**
 * Duration serialized to a string with the format: Xs (e.g. 1s, 2s, 3s, etc.).
 * Vault uses this format to represent durations.
 */
public typealias VaultDuration =
    @Serializable(DurationToVaultPeriodSerializer::class)
    Duration

/**
 * Serializer for [Duration].
 * Serialize the duration to a string with the format: Xs (e.g. 1s, 2s, 3s, etc.).
 * Deserialize the string to a duration by supporting the following formats:
 * - X (e.g. 1, 2, 3, etc.) (interpreted as seconds)
 * - Xs (e.g. 1s, 2s, 3s, etc.)
 * - Xm (e.g. 1m, 2m, 3m, etc.)
 * - Xh (e.g. 1h, 2h, 3h, etc.)
 * - Xd (e.g. 1d, 2d, 3d, etc.)
 */
public object DurationToVaultPeriodSerializer : KSerializer<Duration> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("durationToVaultStringDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Duration {
        val value = decoder.decodeString()
        // Get the unit time: 50s -> s
        val unit = value.last()

        // If the unit is a digit, the duration is in seconds.
        val time: Duration? = if (unit.isDigit()) {
            // Without unit, according to Vault, the duration is in seconds.
            value.toLongOrNull()?.seconds
        } else {
            // Remove the unit time: 50s -> 50
            value.dropLast(1).toLongOrNull()?.let {
                parseDuration(it, unit)
            }
        }

        return time ?: invalidFormat(value)
    }

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeString("${value.inWholeSeconds}s")
    }

    /**
     * Parses the given time and unit to a [Duration].
     * If the unit is 's', the time is interpreted as seconds.
     * If the unit is 'm', the time is interpreted as minutes.
     * If the unit is 'h', the time is interpreted as hours.
     * If the unit is 'd', the time is interpreted as days.
     * @param time Time value.
     * @param unit Unit of the time.
     * @return The parsed duration, or null if the unit is not supported.
     */
    private fun parseDuration(time: Long, unit: Char) = when (unit) {
        's' -> time.seconds
        'm' -> time.minutes
        'h' -> time.hours
        'd' -> time.days
        else -> null
    }

    /**
     * Throws a [SerializationException] with the given duration string to indicate that the format is invalid.
     * @param durationString The duration string that is invalid.
     */
    private fun invalidFormat(durationString: String): Nothing =
        throw SerializationException("Invalid duration format: $durationString, expected format: 1, 1s, 1m, 1h, or 1d")
}
