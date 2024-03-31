package io.github.hansanto.kault.auth.kubernetes.payload

import io.github.hansanto.kault.KaultDsl
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class KubernetesLoginPayload(

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
     * Builder class to simplify the creation of [KubernetesLoginPayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [KubernetesLoginPayload.role]
         */
        public lateinit var role: String

        /**
         * @see [KubernetesLoginPayload.jwt]
         */
        public lateinit var jwt: String

        /**
         * Build the instance of [KubernetesLoginPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): KubernetesLoginPayload = KubernetesLoginPayload(
            role = role,
            jwt = jwt
        )
    }
}
