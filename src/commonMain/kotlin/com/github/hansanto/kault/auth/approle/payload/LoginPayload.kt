package com.github.hansanto.kault.auth.approle.payload

import com.github.hansanto.kault.KaultDsl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class LoginPayload(

    /**
     * RoleID of the AppRole.
     */
    @SerialName("role_id")
    public val roleId: String,

    /**
     * SecretID belonging to AppRole.
     */
    @SerialName("secret_id")
    public val secretId: String

) {

    /**
     * Builder class to simplify the creation of [LoginPayload].
     */
    @KaultDsl
    @Suppress("MemberVisibilityCanBePrivate")
    public class Builder {

        /**
         * @see [LoginPayload.roleId]
         */
        public lateinit var roleId: String

        /**
         * @see [LoginPayload.secretId]
         */
        public lateinit var secretId: String

        /**
         * Build the instance of [LoginPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): LoginPayload = LoginPayload(
            roleId = roleId,
            secretId = secretId
        )
    }
}
