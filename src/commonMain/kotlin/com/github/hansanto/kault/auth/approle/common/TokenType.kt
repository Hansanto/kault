package com.github.hansanto.kault.auth.approle.common

import com.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

/**
 * Enum representing the type of token for AppRole.
 */
@Serializable(TokenTypeSerializer::class)
public enum class TokenType(public val value: String) {
    SERVICE("service"),
    DEFAULT_SERVICE("default-service"),
    BATCH("batch"),
    DEFAULT_BATCH("default-batch"),
    DEFAULT("default");
}

/**
 * Serializer for [TokenType].
 */
public object TokenTypeSerializer : EnumSerializer<TokenType>("tokenType", TokenType.entries, { it.value })
