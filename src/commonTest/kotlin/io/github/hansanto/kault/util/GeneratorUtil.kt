package io.github.hansanto.kault.util

fun randomString(
    allowedChar: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9'),
    size: Int = 50
): String {
    return List(size) { allowedChar.random() }.joinToString("")
}
