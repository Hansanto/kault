package io.github.hansanto.kault.auth.jwt.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

@Serializable(OIDCRoleTypeSerializer::class)
public enum class OIDCRoleType(public val value: String) {
    OIDC("oidc"),
    JWT("jwt"),
}

/**
 * Serializer for [OIDCRoleType].
 */
public object OIDCRoleTypeSerializer : EnumSerializer<OIDCRoleType>(
    "oidcRoleType",
    OIDCRoleType.entries,
    { it.value }
)
