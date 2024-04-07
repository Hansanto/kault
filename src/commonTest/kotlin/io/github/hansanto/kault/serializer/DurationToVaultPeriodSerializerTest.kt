package io.github.hansanto.kault.serializer

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

class DurationToVaultPeriodSerializerTest : FunSpec({

    test("serialize with second unit") {
        assertDurationSerialized(0.seconds, 0)
        assertDurationSerialized(1.seconds, 1)
        assertDurationSerialized(2.seconds, 2)
        assertDurationSerialized(60.seconds, 60)
    }

    test("serialize with minute unit") {
        assertDurationSerialized(0.minutes, 0)
        assertDurationSerialized(1.minutes, 60)
        assertDurationSerialized(2.minutes, 120)
        assertDurationSerialized(60.minutes, 3600)
    }

    test("serialize with hour unit") {
        assertDurationSerialized(0.hours, 0)
        assertDurationSerialized(1.hours, 3600)
        assertDurationSerialized(2.hours, 7200)
        assertDurationSerialized(24.hours, 86400)
    }

    test("serialize with day unit") {
        assertDurationSerialized(0.days, 0)
        assertDurationSerialized(1.days, 86400)
        assertDurationSerialized(2.days, 172800)
    }

    test("deserialize without unit") {
        assertDurationDeserialized("", 0.seconds)
        assertDurationDeserialized("0", 0.seconds)
        assertDurationDeserialized("1", 1.seconds)
        assertDurationDeserialized("2", 2.seconds)
        assertDurationDeserialized("60", 60.seconds)
    }

    test("deserialize with second unit") {
        assertDurationDeserialized("0s", 0.seconds)
        assertDurationDeserialized("1s", 1.seconds)
        assertDurationDeserialized("2s", 2.seconds)
        assertDurationDeserialized("60s", 60.seconds)
    }

    test("deserialize with minute unit") {
        assertDurationDeserialized("0m", 0.minutes)
        assertDurationDeserialized("1m", 1.minutes)
        assertDurationDeserialized("2m", 2.minutes)
        assertDurationDeserialized("60m", 60.minutes)
    }

    test("deserialize with hour unit") {
        assertDurationDeserialized("0h", 0.hours)
        assertDurationDeserialized("1h", 1.hours)
        assertDurationDeserialized("2h", 2.hours)
        assertDurationDeserialized("24h", 24.hours)
    }

    test("deserialize with day unit") {
        assertDurationDeserialized("0d", 0.days)
        assertDurationDeserialized("1d", 1.days)
        assertDurationDeserialized("2d", 2.days)
    }

    test("deserialize with concat unit") {
        assertDurationDeserialized("1m30s", 1.minutes + 30.seconds)
        assertDurationDeserialized("1h30m", 1.hours + 30.minutes)
        assertDurationDeserialized("1d1h", 1.days + 1.hours)
        assertDurationDeserialized("1d1h30m", 1.days + 1.hours + 30.minutes)
        assertDurationDeserialized("1d1h30m30s", 1.days + 1.hours + 30.minutes + 30.seconds)
    }

    test("deserialize with invalid format") {
        assertInvalidFormat("1x")
        assertInvalidFormat("1x2s")
        assertInvalidFormat("1s40m")
        assertInvalidFormat("test")
    }
})

private fun assertDurationSerialized(duration: Duration, expected: Long) {
    val json = """
        "${expected}s"
    """.trimIndent()
    Json.encodeToString(DurationToVaultPeriodSerializer, duration) shouldEqualJson json
}

private fun assertDurationDeserialized(duration: String, expected: Duration) {
    val json = """
        "$duration"
    """.trimIndent()
    Json.decodeFromString(DurationToVaultPeriodSerializer, json) shouldBe expected
}

private fun assertInvalidFormat(duration: String) {
    val json = """
        "$duration"
    """.trimIndent()
    shouldThrow<SerializationException> {
        Json.decodeFromString(DurationToVaultPeriodSerializer, json)
    }
}
