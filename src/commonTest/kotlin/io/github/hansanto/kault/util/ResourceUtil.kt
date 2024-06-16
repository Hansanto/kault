package io.github.hansanto.kault.util

import io.github.hansanto.kault.VaultClient
import kotlinx.io.files.Path

/**
 * Allows searching for a file in the resources test folder.
 * @receiver Path of the file that should be present in the resource folder.
 * @return Resource object to read the file content.
 */
fun String.asResourceFile(): Path {
    val path = Path(workingDirectory(), "src/commonTest/resources/$this")
    if (!path.exists()) {
        throw IllegalStateException("Resource file [$this] is not found")
    }
    return path
}

/**
 * Search a file in the resources test folder and read its content as a string.
 * The content is transformed into an object of type T.
 * @param name Path of the file that should be present in the resource folder.
 * @return Object of type T.
 */
inline fun <reified T> readJson(name: String): T {
    return VaultClient.json.decodeFromString(name.asResourceFile().readLines())
}
