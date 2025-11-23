package io.github.hansanto.kault.identity

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.auth.oidc.VaultAuthOIDCImpl
import io.github.hansanto.kault.identity.entity.VaultIdentityEntity
import io.github.hansanto.kault.identity.entity.VaultIdentityEntityImpl
import io.github.hansanto.kault.identity.oidc.VaultIdentityOIDC
import io.github.hansanto.kault.identity.oidc.VaultIdentityOIDCImpl
import io.ktor.client.HttpClient

/**
 * Service to interact with Vault identity API.
 * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity)
 */
public class VaultIdentity(
    /**
     * Entity management
     */
    public val entity: VaultIdentityEntity,
    /**
     * OIDC provider
     */
    public val oidc: VaultIdentityOIDC,
) {

    public companion object {

        /**
         * Creates a new instance of [VaultIdentity] using the provided HttpClient and optional parent path.
         * @param client HttpClient to interact with API.
         * @param parentPath The optional parent path used for building the final path used to interact with endpoints.
         * @param builder Builder to define the authentication service.
         * @return The instance of [VaultIdentity] that was built.
         */
        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultIdentity = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "identity"
    }

    /**
     * Builder class to simplify the creation of [VaultIdentity].
     */
    public open class Builder : ServiceBuilder<VaultIdentity>() {

        public override var path: String = Default.PATH

        /**
         * Builder to define the entity management.
         */
        private var entityBuilder: BuilderDsl<VaultIdentityEntityImpl.Builder> = {}

        /**
         * Builder to define OIDC provider.
         */
        private var oidcBuilder: BuilderDsl<VaultIdentityOIDCImpl.Builder> = {}

        override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultIdentity = VaultIdentity(
            entity = VaultIdentityEntityImpl.Builder().apply(entityBuilder).build(client, completePath),
            oidc = VaultIdentityOIDCImpl.Builder().apply(oidcBuilder).build(client, completePath)
        )

        /**
         * Sets the entity management.
         *
         * @param builder Builder to create [VaultIdentityEntityImpl] instance.
         */
        public fun entity(builder: BuilderDsl<VaultIdentityEntityImpl.Builder>) {
            entityBuilder = builder
        }

        /**
         * Sets the OIDC provider.
         *
         * @param builder Builder to create [VaultAuthOIDCImpl] instance.
         */
        public fun oidc(builder: BuilderDsl<VaultIdentityOIDCImpl.Builder>) {
            oidcBuilder = builder
        }
    }
}
