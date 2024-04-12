package io.github.hansanto.kault.auth.token.payload

import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.serializer.VaultDuration
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class TokenRenewPayload(

    /**
     * Token to renew. This can be part of the URL or the body.
     */
    @SerialName("token")
    public var token: String,

    /**
     * An optional requested increment duration can be provided. This increment may not be honored, for instance in the case of periodic tokens. If not supplied, Vault will use the default TTL.
     */
    @SerialName("increment")
    public var increment: VaultDuration? = null
) {

    /**
     * Builder class to simplify the creation of [TokenRenewPayload].
     */
    @KaultDsl
    public class Builder {

        /**
         * @see [TokenRenewPayload.token]
         */
        public lateinit var token: String

        /**
         * @see [TokenRenewPayload.increment]
         */
        public var increment: VaultDuration? = null

        /**
         * Build the instance of [TokenRenewPayload] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(): TokenRenewPayload = TokenRenewPayload(
            token = token,
            increment = increment
        )
    }
}
