package io.github.hansanto.kault.engine.kv.v2.response

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.common.SecretVersion
import io.github.hansanto.kault.serializer.VaultDuration
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
public data class KvV2ReadMetadataResponse(

    @SerialName("cas_required")
    val casRequired: Boolean,

    @SerialName("created_time")
    @Contextual
    val createdTime: Instant,

    @SerialName("current_version")
    val currentVersion: SecretVersion,

    @SerialName("custom_metadata")
    val customMetadata: JsonObject?,

    @SerialName("delete_version_after")
    val deleteVersionAfter: VaultDuration,

    @SerialName("max_versions")
    val maxVersions: SecretVersion,

    @SerialName("oldest_version")
    val oldestVersion: SecretVersion,

    @SerialName("updated_time")
    @Contextual
    val updatedTime: Instant,

    @SerialName("versions")
    val versions: Map<SecretVersion, Version>
) {
    @Serializable
    public data class Version(

        /**
         * The time at which the version was created.
         */
        @SerialName("created_time")
        @Contextual
        val createdTime: Instant,

        /**
         * The time at which the version was deleted.
         */
        @SerialName("deletion_time")
        @Serializable(with = OptionalInstantSerializer::class)
        val deletionTime: Instant? = null,

        /**
         * True if the version is destroyed.
         */
        @SerialName("destroyed")
        val destroyed: Boolean
    )

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
