package io.github.hansanto.kault.auth.oidc.common

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
    OIDCBoundClaimsType::class.qualifiedName!!,
    OIDCBoundClaimsType.entries,
    { it.value }
)
