package io.github.hansanto.kault.identity.oidc.response

import io.github.hansanto.kault.VaultClient
import io.ktor.util.decodeBase64String
import kotlinx.serialization.KSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Serializable
public data class OIDCReadScopeResponse(
    /**
     * The [JSON template](https://developer.hashicorp.com/vault/docs/concepts/oidc-provider#scopes) string for the scope. This may be provided as escaped JSON or base64 encoded JSON.
     */
    @SerialName("template")
    public var template: String,

    /**
     * A description of the scope.
     */
    @SerialName("description")
    public var description: String,
) {

    /**
     * Decode the [template] to [T] using [format] and [serializer].
     * @param base64 Indicates if the [template] field is base64 encoded.
     * @param format Format to use to decode the [template] field.
     * @param serializer Serializer to indicate the strategy to decode the [template] field.
     * @return The decoded [template] field.
     */
    public inline fun <reified T> template(
        base64: Boolean = false,
        format: Json = VaultClient.json,
        serializer: KSerializer<T> = format.serializersModule.serializer<T>()
    ): T {
        return if (base64) {
            format.decodeFromString(serializer, this.template.decodeBase64String())
        } else {
            format.decodeFromString(serializer, this.template)
        }
    }
}
