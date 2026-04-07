package io.github.hansanto.kault.auth.kubernetes.payload

import io.github.hansanto.kault.KaultDsl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AuthKubernetesLoginPayload(

    /**
     * Name of the role against which the login is being attempted.
     */
    @SerialName("role")
    public var role: String,

    /**
     * Signed JSON Web Token (JWT) for authenticating a service account.
     */
    @SerialName("jwt")
    public var jwt: String

) {

    /**
     * Builder class to simplify the creation of [AuthKubernetesLoginPayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [AuthKubernetesLoginPayload.role]
         */
        public lateinit var role: String

        /**
         * @see [AuthKubernetesLoginPayload.jwt]
         */
        public lateinit var jwt: String

        /**
         * Build the instance of [AuthKubernetesLoginPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): AuthKubernetesLoginPayload = AuthKubernetesLoginPayload(
            role = role,
            jwt = jwt
        )
    }
}
