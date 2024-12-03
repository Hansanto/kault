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
    /**
     * Symbol to find the day unit.
     */
    private const val DAY_UNIT = 'd'

    /**
     * Symbol to find the hour unit.
     */
    private const val HOUR_UNIT = 'h'

    /**
     * Symbol to find the minute unit.
     */
    private const val MINUTE_UNIT = 'm'

    /**
     * Symbol to find the second unit.
     */
    private const val SECOND_UNIT = 's'

    /**
     * Regular expression pattern for matching time durations.
     *
     * The pattern matches the following format:
     * - Optional days value followed by the "[DAY_UNIT]"
     * - Optional hours value followed by the "[HOUR_UNIT]"
     * - Optional minutes value followed by the "[MINUTE_UNIT]"
     * - Optional seconds value followed by the "[SECOND_UNIT]" or nothing.
     * Vault can send a duration without a unit, in which case it is interpreted as seconds.
     */
    @Suppress("ktlint:standard:max-line-length")
    private val timeRegex =
        Regex(
            """^(?:(?<days>\d+)$DAY_UNIT)?(?:(?<hours>\d+)$HOUR_UNIT)?(?:(?<minutes>\d+)$MINUTE_UNIT)?(?:(?<seconds>\d+)$SECOND_UNIT?)?$"""
        )

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("durationToVaultStringDate", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Duration {
        val decoded = decoder.decodeString()
        val matcher = timeRegex.matchEntire(decoded)

        val (days, hours, minutes, seconds) = matcher?.destructured ?: invalidFormat(decoded)

        var time = Duration.ZERO
        if (days.isNotEmpty()) {
            time += days.toLongOrNull()?.days ?: invalidFormat(decoded, days, DAY_UNIT)
        }
        if (hours.isNotEmpty()) {
            time += hours.toLongOrNull()?.hours ?: invalidFormat(decoded, hours, HOUR_UNIT)
        }
        if (minutes.isNotEmpty()) {
            time += minutes.toLongOrNull()?.minutes ?: invalidFormat(decoded, minutes, MINUTE_UNIT)
        }
        if (seconds.isNotEmpty()) {
            time += seconds.toLongOrNull()?.seconds ?: invalidFormat(decoded, seconds, SECOND_UNIT)
        }
        return time
    }

    override fun serialize(encoder: Encoder, value: Duration) {
        encoder.encodeString("${value.inWholeSeconds}s")
    }

    /**
     * Throws a [SerializationException] with the given duration string to indicate that the format is invalid.
     * @param decodedString The duration string that is invalid.
     * @param unitValue The value of the unit that is invalid.
     * @param unit The unit that is invalid.
     */
    private fun invalidFormat(decodedString: String, unitValue: String, unit: Char): Nothing =
        throw SerializationException(
            "Invalid duration format [$decodedString] with value [$unitValue] for unit [$unit]."
        )

    /**
     * Throws a [SerializationException] with the given duration string to indicate that the format is invalid.
     * @param durationString The duration string that is invalid.
     */
    private fun invalidFormat(durationString: String): Nothing =
        throw SerializationException("Invalid duration format [$durationString]")
}
