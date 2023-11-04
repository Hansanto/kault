package com.github.hansanto.kault.util

import com.github.hansanto.kault.VaultClient
import com.goncalossilva.resources.Resource

inline fun <reified T> readJson(name: String): T {
    return VaultClient.json.decodeFromString(Resource("src/commonTest/resources/$name").readText())
}
