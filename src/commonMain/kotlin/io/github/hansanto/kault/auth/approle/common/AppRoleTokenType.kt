package io.github.hansanto.kault.auth.approle.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

/**
 * Enum representing the type of token for AppRole.
 */
@Serializable(AppRoleTokenTypeSerializer::class)
public enum class AppRoleTokenType(public val value: String) {
    SERVICE("service"),
    DEFAULT_SERVICE("default-service"),
    BATCH("batch"),
    DEFAULT_BATCH("default-batch"),
    DEFAULT("default");
}

/**
 * Serializer for [AppRoleTokenType].
 */
public object AppRoleTokenTypeSerializer : EnumSerializer<AppRoleTokenType>(
    "appRoleTokenType",
    AppRoleTokenType.entries,
    { it.value }
)
