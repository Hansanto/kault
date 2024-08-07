package io.github.hansanto.kault.util

import com.goncalossilva.resources.Resource
import io.github.hansanto.kault.VaultClient

/**
 * Allows searching for a file in the resources test folder.
 * @receiver Path of the file that should be present in the resource folder.
 * @return Resource object to read the file content.
 */
fun String.asResourceFile(): Resource {
    return Resource("src/commonTest/resources/$this")
}

/**
 * Search a file in the resources test folder and read its content as a string.
 * The content is transformed into an object of type T.
 * @param name Path of the file that should be present in the resource folder.
 * @return Object of type T.
 */
inline fun <reified T> readJson(name: String): T {
    return VaultClient.json.decodeFromString(name.asResourceFile().readText())
}
