package io.github.hansanto.kault.engine.kv.v2.payload

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.approle.payload.AppRoleCreateCustomSecretIDPayload
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer

@Serializable
public data class KvV2WriteRequest(

    /**
     * The contents of the data map will be stored and returned on read.
     */
    @SerialName("data")
    var data: JsonObject,

    /**
     * An object that holds option settings.
     */
    @SerialName("options")
    var options: Options? = null

) {

    @Serializable
    public data class Options(
        /**
         * This flag is required if cas_required is set to true on either the secret or the engine's config. If not set the write will be allowed. In order for a write to be successful, cas must be set to the current version of the secret. If set to 0 a write will only be allowed if the key doesn't exist as unset keys do not have any version information. Also remember that soft deletes do not remove any underlying version data from storage. In order to write to a soft deleted key, the cas parameter must match the key's current version.
         */
        @SerialName("cas")
        public var cas: Int? = null
    )

    /**
     * Builder class to simplify the creation of [AppRoleCreateCustomSecretIDPayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [KvV2WriteRequest.data]
         */
        public lateinit var data: JsonObject

        /**
         * Builder to define the [Options] of [KvV2WriteRequest].
         */
        private var optionsBuilder: BuilderDsl<Options>? = null

        /**
         * Build the instance of [KvV2WriteRequest] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): KvV2WriteRequest = KvV2WriteRequest(
            data = data,
            options = optionsBuilder?.let { Options().apply(it) }
        )

        /**
         * Sets the option builder.
         *
         * @param builder Builder to create [Options] instance.
         */
        public fun options(builder: BuilderDsl<Options>) {
            optionsBuilder = builder
        }

        /**
         * Encode the [data] using [format] and [serializer].
         * The encoded value will be stored in [Builder.data]
         * @param data Data to encode.
         * @param format Format to use to decode the [data] field.
         * @param serializer Serializer to indicate the strategy to decode the [data] field.
         */
        public inline fun <reified T> data(
            data: T,
            format: Json = VaultClient.json,
            serializer: KSerializer<T> = format.serializersModule.serializer<T>()
        ) {
            this.data = format.encodeToJsonElement(serializer, data).jsonObject
        }
    }
}
