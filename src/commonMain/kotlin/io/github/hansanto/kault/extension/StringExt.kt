package io.github.hansanto.kault.extension

/**
 * Appends a child path to the current URL.
 * Remove the extra [URL_PATH_SEPARATOR] from the parent path and the child path
 * before appending them with a [URL_PATH_SEPARATOR].
 * Example:
 * ```kotlin
 * "http://localhost:8200".addURLChildPath("v1") // "http://localhost:8200/v1"
 * "http://localhost:8200/".addURLChildPath("v1") // "http://localhost:8200/v1"
 * "http://localhost:8200".addURLChildPath("/v1") // "http://localhost:8200/v1"
 * "http://localhost:8200/".addURLChildPath("/v1") // "http://localhost:8200/v1"
 * "http://localhost:8200".addURLChildPath("/v1/") // "http://localhost:8200/v1"
 * ```
 *
 * @receiver The parent URL.
 * @param path The child path to append.
 * @return String with the child path appended.
 */
public fun String.addURLChildPath(path: String): String {
    val parentPathWithoutSeparator = removeSuffix(URL_PATH_SEPARATOR)
    val pathWithoutSeparator = path.removeSuffix(URL_PATH_SEPARATOR).removePrefix(URL_PATH_SEPARATOR)
    return parentPathWithoutSeparator + URL_PATH_SEPARATOR + pathWithoutSeparator
}
