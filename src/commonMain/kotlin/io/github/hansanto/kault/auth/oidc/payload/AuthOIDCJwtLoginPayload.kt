package io.github.hansanto.kault.auth.oidc.payload

import io.github.hansanto.kault.KaultDsl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AuthOIDCJwtLoginPayload(
    /**
     * Signed [JSON Web Token](https://tools.ietf.org/html/rfc7519) (JWT).
     */
    @SerialName("jwt")
    public var jwt: String,

    /**
     * Name of the role against which the login is being attempted. Defaults to configured default_role if not provided.
     */
    @SerialName("role")
    public var role: String? = null,

) {

    /**
     * Builder class to simplify the creation of [AuthOIDCJwtLoginPayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [AuthOIDCJwtLoginPayload.jwt]
         */
        public lateinit var jwt: String

        /**
         * @see [AuthOIDCJwtLoginPayload.role]
         */
        public var role: String? = null

        /**
         * Build the instance of [AuthOIDCJwtLoginPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): AuthOIDCJwtLoginPayload = AuthOIDCJwtLoginPayload(
            jwt = jwt,
            role = role,
        )
    }
}
