package com.github.hansanto.kault.auth.approle.common

import com.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

/**
 * Enum representing the type of token for AppRole.
 */
@Serializable(TokenTypeSerializer::class)
public enum class TokenType {
    SERVICE,
    BATCH,
    DEFAULT;
}

/**
 * Serializer for [TokenType].
 * Use the name of the enum as the serialized value after converting it to lowercase.
 */
public object TokenTypeSerializer : EnumSerializer<TokenType>("tokenType", TokenType.entries, { it.name.lowercase() })
