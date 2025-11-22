package io.github.hansanto.kault.identity.oidc.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

@Serializable(IdentityOIDCResponseTypeSerializer::class)
public enum class IdentityOIDCResponseType(public val value: String) {
    CODE("code")
}

/**
 * Serializer for [IdentityOIDCResponseType].
 */
public object IdentityOIDCResponseTypeSerializer : EnumSerializer<IdentityOIDCResponseType>(
    "identityOIDCResponseType",
    IdentityOIDCResponseType.entries,
    { it.value }
)
