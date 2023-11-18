package io.github.hansanto.kault.util

import com.goncalossilva.resources.Resource
import io.github.hansanto.kault.VaultClient

inline fun <reified T> readJson(name: String): T {
    return VaultClient.json.decodeFromString(Resource("src/commonTest/resources/$name").readText())
}
