package io.github.hansanto.kault.util

import kotlinx.datetime.Instant

const val DEFAULT_ROLE_NAME = "test"

fun randomString(
    allowedChar: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9'),
    size: Int = 50
): String {
    return List(size) { allowedChar.random() }.joinToString("")
}

fun randomBoolean(): Boolean {
    return listOf(true, false).random()
}

fun randomLong(range: LongRange = 0L..100L): Long {
    return range.random()
}

fun randomInstant(): Instant {
    return Instant.fromEpochMilliseconds(
        randomLong(Instant.DISTANT_FUTURE.toEpochMilliseconds()..Instant.DISTANT_FUTURE.toEpochMilliseconds())
    )
}
