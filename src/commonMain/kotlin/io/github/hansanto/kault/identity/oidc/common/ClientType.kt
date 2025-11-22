package io.github.hansanto.kault.identity.oidc.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

/**
 * Enum representing the type of OIDC client.
 */
@Serializable(ClientTypeSerializer::class)
public enum class ClientType(public val value: String) {
    CONFIDENTIAL("confidential"),
    PUBLIC("public")
}

/**
 * Serializer for [ClientType].
 */
public object ClientTypeSerializer : EnumSerializer<ClientType>(
    "clientType",
    ClientType.entries,
    { it.value }
)
