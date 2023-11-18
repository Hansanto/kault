package io.github.hansanto.kault.extension

import kotlinx.serialization.KSerializer
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.json.Json

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
public fun <K, V> Map<K, V>.toJsonString(
    keySerializer: KSerializer<K>,
    valueSerializer: KSerializer<V>
): String {
    val serializer = MapSerializer(keySerializer, valueSerializer)
    return Json.encodeToString(serializer, this)
}
