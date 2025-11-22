package io.github.hansanto.kault.extension

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive

/**
 * Convert a Map to a JSON string.
 * Example:
 * ```kotlin
 * val map = mapOf("key1" to "value1", "key2" to "value2")
 * // "{\"key1\":\"value1\",\"key2\":\"value2\"}"
 * val jsonString = map.toJsonString(String.serializer(), String.serializer())
 * ```
 * @receiver Map to convert to JSON string.
 * @param keySerializer Serializer for the key.
 * @param valueSerializer Serializer for the value.
 * @return JSON string corresponding to the Map.
 */
public fun <K, V> Map<K, V>.toJsonString(keySerializer: KSerializer<K>, valueSerializer: KSerializer<V>): String {
    val serializer = MapSerializer(keySerializer, valueSerializer)
    return Json.encodeToString(serializer, this)
}

/**
 * Transform [Any] to [JsonPrimitive].
 * Examples:
 * ```kotlin
 * null.toJsonPrimitive() // JsonPrimitive(null)
 * "string".toJsonPrimitive() // JsonPrimitive("string")
 * 42.toJsonPrimitive() // JsonPrimitive(42)
 * true.toJsonPrimitive() // JsonPrimitive(true)
 * (3.14).toJsonPrimitive() // JsonPrimitive(3.14)
 * listOf(1, 2, 3).toJsonPrimitive() // JsonPrimitive("[1, 2, 3]")
 * (mapOf("key" to "value")).toJsonPrimitive() // JsonPrimitive("{key=value}")
 * data class Person(val name: String, val age: Int)
 * Person("Alice", 30).toJsonPrimitive() // JsonPrimitive("Person(name=Alice, age=30)")
 * ```
 * @return [JsonPrimitive] representation of the object.
 */
public fun Any?.toJsonPrimitive(): JsonPrimitive = when (this) {
    null -> JsonPrimitive(null)
    is String -> JsonPrimitive(this)
    is Number -> JsonPrimitive(this)
    is Boolean -> JsonPrimitive(this)
    is JsonPrimitive -> this
    else -> JsonPrimitive(this.toString())
}

/**
 * Transform a Map<String, Any?> to Map<String, JsonPrimitive>.
 *
 * @return Map with values converted to [JsonPrimitive].
 * @see [toJsonPrimitive]
 */
public fun Map<String, Any?>.toJsonPrimitiveMap(): Map<String, JsonPrimitive> =
    mapValues { (_, value) -> value.toJsonPrimitive() }
