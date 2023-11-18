package io.github.hansanto.kault.exception

/**
 * Exception indicating that an error occurred while interacting with the Vault API.
 */
public open class VaultException(message: String) : RuntimeException(message)

/**
 * Exception indicating that an error occurred while interacting with the Vault API.
 * This exception is thrown when the response status code is not 2xx.
 * @property errors List of errors returned by the Vault API.
 */
public class VaultAPIException(public val errors: List<String> = emptyList()) : VaultException(errors.joinToString())
