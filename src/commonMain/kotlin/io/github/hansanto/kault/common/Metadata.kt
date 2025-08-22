package io.github.hansanto.kault.common

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.serializer.optional.OptionalInstantSerializer
import kotlinx.serialization.Contextual
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.serializer
import kotlin.time.Instant

@Serializable
public data class Metadata(

    /**
     * The time at which the version was created.
     */
    @SerialName("created_time")
    @Contextual
    val createdTime: Instant,

    /**
     * The custom metadata.
     */
    @SerialName("custom_metadata")
    val customMetadata: JsonObject?,

    /**
     * The time at which the version was deleted.
     */
    @SerialName("deletion_time")
    @Serializable(with = OptionalInstantSerializer::class)
    val deletionTime: Instant?,

    /**
     * True if the version is destroyed.
     */
    @SerialName("destroyed")
    val destroyed: Boolean,

    /**
     * Version of the secret.
     */
    @SerialName("version")
    val version: SecretVersion
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
