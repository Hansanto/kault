package io.github.hansanto.kault.auth.approle.payload

import io.github.hansanto.kault.KaultDsl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AppRoleLoginPayload(

    /**
     * RoleID of the AppRole.
     */
    @SerialName("role_id")
    public var roleId: String,

    /**
     * SecretID belonging to AppRole.
     */
    @SerialName("secret_id")
    public var secretId: String

) {

    /**
     * Builder class to simplify the creation of [AppRoleLoginPayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [AppRoleLoginPayload.roleId]
         */
        public lateinit var roleId: String

        /**
         * @see [AppRoleLoginPayload.secretId]
         */
        public lateinit var secretId: String

        /**
         * Build the instance of [AppRoleLoginPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): AppRoleLoginPayload = AppRoleLoginPayload(
            roleId = roleId,
            secretId = secretId
        )
    }
}
