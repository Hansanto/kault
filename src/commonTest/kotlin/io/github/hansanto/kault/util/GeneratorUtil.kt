package io.github.hansanto.kault.util

import kotlin.time.Instant

const val DEFAULT_ROLE_NAME = "test"

fun randomString(allowedChar: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9'), size: Int = 50): String = List(size) {
    allowedChar.random()
}.joinToString("")

fun randomBoolean(): Boolean = listOf(true, false).random()

fun randomLong(range: LongRange = 0L..100L): Long = range.random()

fun randomInstant(): Instant = Instant.fromEpochMilliseconds(
    randomLong(Instant.DISTANT_FUTURE.toEpochMilliseconds()..Instant.DISTANT_FUTURE.toEpochMilliseconds())
)
