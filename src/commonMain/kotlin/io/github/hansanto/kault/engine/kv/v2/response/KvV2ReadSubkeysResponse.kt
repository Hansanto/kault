package io.github.hansanto.kault.engine.kv.v2.response

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.common.Metadata
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

@Serializable
public data class KvV2ReadSubkeysResponse(

    /**
     * Metadata contains the information relative to the secret version.
     */
    @SerialName("metadata")
    val metadata: Metadata,

    /**
     * The key-value pair data.
     * The value has an arbitrary structure, with key's values set to null.
     * ```json
     * {
     *     "foo": null,
     *     "bar": {
     *       "baz": null
     *     },
     *     "quux": null
     * }
     * ```
     */
    @SerialName("subkeys")
    val subKeys: JsonObject

) {

    /**
     * Decode the [subKeys] to [T] using [format] and [serializer].
     * @param format Format to use to decode the [subKeys] field.
     * @param serializer Serializer to indicate the strategy to decode the [subKeys] field.
     * @return The decoded [subKeys] field.
     */
    public inline fun <reified T> subKeys(
        format: Json = VaultClient.json,
        serializer: KSerializer<T> = format.serializersModule.serializer<T>()
    ): T = format.decodeFromJsonElement(serializer, subKeys)
}
