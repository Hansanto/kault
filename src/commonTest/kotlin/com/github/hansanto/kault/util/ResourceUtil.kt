package com.github.hansanto.kault.util

import com.goncalossilva.resources.Resource
import kotlinx.serialization.json.Json

inline fun <reified T> readJson(name: String): T {
    return Json.decodeFromString(Resource("src/commonTest/resources/$name").readText())
}
