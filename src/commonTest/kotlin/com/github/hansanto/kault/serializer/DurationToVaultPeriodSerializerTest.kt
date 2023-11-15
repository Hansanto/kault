package com.github.hansanto.kault.serializer

import io.kotest.assertions.json.shouldEqualJson
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

class DurationToVaultPeriodSerializerTest : FunSpec({

    test("serialize with second unit") {
        assertDurationSerialized(0.seconds, 0)
        assertDurationSerialized(1.seconds, 1)
        assertDurationSerialized(2.seconds, 2)
    }

    test("serialize with minute unit") {
        assertDurationSerialized(0.minutes, 0)
        assertDurationSerialized(1.minutes, 60)
        assertDurationSerialized(2.minutes, 120)
    }

    test("serialize with hour unit") {
        assertDurationSerialized(0.hours, 0)
        assertDurationSerialized(1.hours, 3600)
        assertDurationSerialized(2.hours, 7200)
    }

    test("serialize with day unit") {
        assertDurationSerialized(0.days, 0)
        assertDurationSerialized(1.days, 86400)
        assertDurationSerialized(2.days, 172800)
    }

    test("deserialize without unit") {
        assertDurationDeserialized(0.seconds, "0")
        assertDurationDeserialized(1.seconds, "1")
        assertDurationDeserialized(2.seconds, "2")
    }

    test("deserialize with second unit") {
        assertDurationDeserialized(0.seconds, "0s")
        assertDurationDeserialized(1.seconds, "1s")
        assertDurationDeserialized(2.seconds, "2s")
    }

    test("deserialize with minute unit") {
        assertDurationDeserialized(0.minutes, "0m")
        assertDurationDeserialized(1.minutes, "1m")
        assertDurationDeserialized(2.minutes, "2m")
    }

    test("deserialize with hour unit") {
        assertDurationDeserialized(0.hours, "0h")
        assertDurationDeserialized(1.hours, "1h")
        assertDurationDeserialized(2.hours, "2h")
    }

    test("deserialize with day unit") {
        assertDurationDeserialized(0.days, "0d")
        assertDurationDeserialized(1.days, "1d")
        assertDurationDeserialized(2.days, "2d")
    }

    test("deserialize with invalid format") {
        assertInvalidFormat("1x")
        assertInvalidFormat("1x2s")
        assertInvalidFormat("1s40m")
        assertInvalidFormat("1m3h")
        assertInvalidFormat("1h1d")
        assertInvalidFormat("1d1")
    }
})

private fun assertDurationSerialized(duration: Duration, expected: Long) {
    val json = """
        "${expected}s"
    """.trimIndent()
    Json.encodeToString(DurationToVaultPeriodSerializer, duration) shouldEqualJson json
}

private fun assertDurationDeserialized(duration: Duration, expected: String) {
    val json = """
        "$expected"
    """.trimIndent()
    Json.decodeFromString(DurationToVaultPeriodSerializer, json) shouldBe duration
}

private fun assertInvalidFormat(duration: String) {
    val json = """
        "$duration"
    """.trimIndent()
    shouldThrow<SerializationException> {
        Json.decodeFromString(DurationToVaultPeriodSerializer, json)
    }
}
