package io.github.hansanto.kault.auth.token.payload

import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AuthTokenRenewAccessorTokenPayload(

    /**
     * Accessor associated with the token to renew.
     */
    @SerialName("accessor")
    public var accessor: String,

    /**
     * An optional requested increment duration can be provided. This increment may not be honored, for instance in the case of periodic tokens. If not supplied, Vault will use the default TTL.
     */
    @SerialName("increment")
    public var increment: VaultDuration? = null
) {

    /**
     * Builder class to simplify the creation of [AuthTokenRenewAccessorTokenPayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [AuthTokenRenewAccessorTokenPayload.accessor]
         */
        public lateinit var accessor: String

        /**
         * @see [AuthTokenRenewAccessorTokenPayload.increment]
         */
        public var increment: VaultDuration? = null

        /**
         * Build the instance of [AuthTokenRenewAccessorTokenPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): AuthTokenRenewAccessorTokenPayload = AuthTokenRenewAccessorTokenPayload(
            accessor = accessor,
            increment = increment
        )
    }
}
