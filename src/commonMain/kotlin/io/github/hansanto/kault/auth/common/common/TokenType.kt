package io.github.hansanto.kault.auth.common.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

/**
 * Enum representing the type of token.
 */
@Serializable(TypeSerializer::class)
public enum class TokenType(public val value: String) {
    SERVICE("service"),
    DEFAULT_SERVICE("default-service"),
    BATCH("batch"),
    DEFAULT_BATCH("default-batch"),
    DEFAULT("default")
}

/**
 * Serializer for [TokenType].
 */
public object TypeSerializer : EnumSerializer<TokenType>(
    "tokenType",
    TokenType.entries,
    { it.value }
)
