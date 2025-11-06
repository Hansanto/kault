package io.github.hansanto.kault.auth.jwt.response

import io.github.hansanto.kault.auth.common.common.TokenType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCReadRoleResponse(
    /**
     * The type of token to generate.
     */
    @SerialName("token_type")
    val tokenType: TokenType

)
