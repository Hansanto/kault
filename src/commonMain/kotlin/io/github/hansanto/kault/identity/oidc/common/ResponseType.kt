package io.github.hansanto.kault.identity.oidc.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

@Serializable(ResponseTypeSerializer::class)
public enum class ResponseType(public val value: String) {
    CODE("code")
}

/**
 * Serializer for [ResponseType].
 */
public object ResponseTypeSerializer : EnumSerializer<ResponseType>(
    ResponseType::class.qualifiedName!!,
    ResponseType.entries,
    { it.value }
)
