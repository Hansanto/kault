package io.github.hansanto.kault.identity.oidc.payload

import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.VaultClient
import io.ktor.util.encodeBase64
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.serializer

@Serializable
public data class OIDCCreateOrUpdateScopePayload(
    /**
     * The [JSON template](https://developer.hashicorp.com/vault/docs/concepts/oidc-provider#scopes) string for the scope. This may be provided as escaped JSON or base64 encoded JSON.
     */
    @SerialName("template")
    public var template: String? = null,

    /**
     * A description of the scope.
     */
    @SerialName("description")
    public var description: String? = null,
) {

    /**
     * Builder class to simplify the creation of [OIDCCreateOrUpdateScopePayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [OIDCCreateOrUpdateScopePayload.template]
         */
        public var template: String? = null

        /**
         * @see [OIDCCreateOrUpdateScopePayload.description]
         */
        public var description: String? = null

        /**
         * Build the instance of [OIDCCreateOrUpdateScopePayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): OIDCCreateOrUpdateScopePayload = OIDCCreateOrUpdateScopePayload(
            template = this@Builder.template,
            description = description
        )

        /**
         * The field [template] can be a stringifies JSON object or a base64 encoded JSON object.
         * This function helps to encode the [template] field from an object
         * @param data Data to encode.
         * @param base64 `true` to encode the data in base64, `false` to use plain JSON.
         * @param format Format to use to encode the [data] field.
         * @param serializer Serializer to indicate the strategy to decode the [data] field.
         */
        public inline fun <reified T> template(
            data: T,
            base64: Boolean = false,
            format: Json = VaultClient.json,
            serializer: KSerializer<T> = format.serializersModule.serializer<T>()
        ) {
            val encoded = format.encodeToString(serializer, data)
            this.template = if (base64) {
                encoded.encodeBase64()
            } else {
                encoded
            }
        }
    }
}
