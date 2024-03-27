package io.github.hansanto.kault.response

/**
 * Represents the fields used in the response from the Vault API.
 */
public object ResponseFields {
    /**
     * Name of the field that contains the data information.
     */
    public const val DATA: String = "data"

    /**
     * Name of the field that contains the authentication information.
     */
    public const val AUTH: String = "auth"

    /**
     * Name of the field that contains the warning information.
     */
    public const val WARNINGS: String = "warnings"
}
