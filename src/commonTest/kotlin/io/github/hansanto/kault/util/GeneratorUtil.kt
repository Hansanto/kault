package io.github.hansanto.kault.util

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
