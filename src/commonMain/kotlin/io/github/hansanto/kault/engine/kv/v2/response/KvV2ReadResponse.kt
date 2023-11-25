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
public data class KvV2ReadResponse(

    /**
     * The key-value pair data.
     * The value has an arbitrary structure, encoded using JSON.
     */
    @SerialName("data")
    val data: JsonObject?,

    /**
     * Metadata contains the information relative to the secret version.
     */
    @SerialName("metadata")
    val metadata: Metadata

) {

    /**
     * Decode the [data] to [T] using [format] and [serializer].
     * @param format Format to use to decode the [data] field.
     * @param serializer Serializer to indicate the strategy to decode the [data] field.
     * @return The decoded [data] field.
     */
    public inline fun <reified T> data(
        format: Json = VaultClient.json,
        serializer: KSerializer<T> = format.serializersModule.serializer<T>()
    ): T? = data?.let { format.decodeFromJsonElement(serializer, it) }
}
