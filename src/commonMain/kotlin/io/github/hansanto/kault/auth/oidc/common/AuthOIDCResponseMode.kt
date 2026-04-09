package io.github.hansanto.kault.auth.oidc.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

@Serializable(OIDCResponseModeSerializer::class)
public enum class OIDCResponseMode(public val value: String) {
    NONE(""),
    QUERY("query"),
    FORM_POST("form_post"),
}

/**
 * Serializer for [OIDCResponseMode].
 */
public object OIDCResponseModeSerializer : EnumSerializer<OIDCResponseMode>(
    "kault.auth.oidc.common.OIDCResponseModeSerializer",
    OIDCResponseMode.entries,
    { it.value }
)
