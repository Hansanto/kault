package io.github.hansanto.kault.engine.kv.v2.response

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer

@Serializable
public data class KvV2WriteResponse(

    @SerialName("created_time")
    val createdTime: VaultDuration,

    @SerialName("custom_metadata")
    val customMetadata: JsonObject? = null,

    @SerialName("deletion_time")
    val deletionTime: String,

    @SerialName("destroyed")
    val destroyed: Boolean,

    @SerialName("version")
    val version: Long

) {

    /**
     * Decode the [customMetadata] to [T] using [format] and [serializer].
     * @param format Format to use to decode the [customMetadata] field.
     * @param serializer Serializer to indicate the strategy to decode the [customMetadata] field.
     * @return The decoded [customMetadata] field.
     */
    public inline fun <reified T> customMetadata(
        format: Json = VaultClient.json,
        serializer: KSerializer<T> = format.serializersModule.serializer<T>()
    ): T? = customMetadata?.let { format.decodeFromJsonElement(serializer, it) }
}
