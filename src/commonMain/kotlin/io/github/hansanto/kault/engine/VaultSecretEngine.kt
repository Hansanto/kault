package io.github.hansanto.kault.engine

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.KaultDsl
import io.github.hansanto.kault.engine.kv.v2.VaultKV2Engine
import io.github.hansanto.kault.engine.kv.v2.VaultKV2EngineImpl
import io.ktor.client.HttpClient

/**
 * Service to interact with Vault Secret Engine API.
 */
public class VaultSecretEngine(
    public val kv2: VaultKV2Engine
) {

    public companion object {

        /**
         * Creates a new instance of [VaultSecretEngine] using the provided HttpClient and optional parent path.
         * @param client HttpClient to interact with API.
         * @param builder Builder to define the authentication service.
         * @return The instance of [VaultSecretEngine] that was built.
         */
        public inline operator fun invoke(
            client: HttpClient,
            builder: BuilderDsl<Builder>
        ): VaultSecretEngine = Builder().apply(builder).build(client)
    }

    /**
     * Builder class to simplify the creation of [VaultSecretEngine].
     */
    @KaultDsl
    public class Builder {

        /**
         * Builder to define the key-value version 2 service.
         */
        private var kv2Builder: BuilderDsl<VaultKV2EngineImpl.Builder> = {}

        /**
         * Build the instance of [VaultSecretEngine] with the values defined in builder.
         * @return A new instance.
         */
        public fun build(client: HttpClient): VaultSecretEngine {
            return VaultSecretEngine(
                kv2 = VaultKV2EngineImpl.Builder().apply(kv2Builder).build(client)
            )
        }

        /**
         * Sets the key-value version 2 service builder.
         *
         * @param builder Builder to create [VaultKV2Engine] instance.
         */
        public fun kv2(builder: BuilderDsl<VaultKV2EngineImpl.Builder>) {
            kv2Builder = builder
        }
    }
}
