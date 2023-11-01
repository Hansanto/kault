package com.github.hansanto.kault.exception

import kotlinx.serialization.Serializable

/**
 * Represents a response from the Vault API that indicates an error occurred.
 *
 * @property errors The array of error messages returned by the Vault API.
 */
@Serializable
public class VaultErrorResponse(public val errors: Array<String>)

/**
 * Exception indicating that an error occurred while interacting with the Vault API.
 */
public open class VaultException(message: String) : RuntimeException(message)

/**
 * Exception indicating that an error occurred while interacting with the Vault API.
 * This exception is thrown when the response status code is not 2xx.
 * @property errors List of errors returned by the Vault API.
 */
public class VaultAPIException(public val errors: Array<String>) : VaultException(errors.joinToString())
