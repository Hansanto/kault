package io.github.hansanto.kault.auth.jwt.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

@Serializable(OIDCResponseTypeSerializer::class)
public enum class OIDCResponseType(public val value: String) {
    CODE("code"),
    ID_TOKEN("id_token"),
}

/**
 * Serializer for [OIDCResponseType].
 */
public object OIDCResponseTypeSerializer : EnumSerializer<OIDCResponseType>(
    "oidcResponseType",
    OIDCResponseType.entries,
    { it.value }
)
