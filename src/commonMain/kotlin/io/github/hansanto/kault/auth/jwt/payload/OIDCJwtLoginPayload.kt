package io.github.hansanto.kault.auth.jwt.payload

import io.github.hansanto.kault.KaultDsl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCJwtLoginPayload(
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
     * Builder class to simplify the creation of [OIDCJwtLoginPayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [OIDCJwtLoginPayload.jwt]
         */
        public lateinit var jwt: String

        /**
         * @see [OIDCJwtLoginPayload.role]
         */
        public var role: String? = null

        /**
         * Build the instance of [OIDCJwtLoginPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): OIDCJwtLoginPayload = OIDCJwtLoginPayload(
            jwt = jwt,
            role = role,
        )
    }
}
