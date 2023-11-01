package com.github.hansanto.kault.exception

import kotlinx.serialization.Serializable

@Serializable
public class VaultErrorResponse(
    public val errors: Array<String>
)

public class VaultException(public val errors: Array<String>) : RuntimeException(errors.joinToString())
