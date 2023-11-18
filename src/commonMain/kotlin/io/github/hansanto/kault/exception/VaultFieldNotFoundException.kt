package io.github.hansanto.kault.exception

/**
 * Exception indicating that a specific field was not found in the response body.
 *
 * @property field The name of the field that was not found.
 */
public class VaultFieldNotFoundException(public val field: String) :
    RuntimeException("The field [$field] was not found in the response body.")
