package io.github.hansanto.kault.auth.jwt.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

@Serializable(OIDCBoundClaimsTypeSerializer::class)
public enum class OIDCBoundClaimsType(public val value: String) {
    STRING("string"),
    GLOB("glob"),
}

/**
 * Serializer for [OIDCBoundClaimsType].
 */
public object OIDCBoundClaimsTypeSerializer : EnumSerializer<OIDCBoundClaimsType>(
    "oidcBoundClaimsType",
    OIDCBoundClaimsType.entries,
    { it.value }
)
