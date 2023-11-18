package io.github.hansanto.kault.engine.kv.v2

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.ktor.client.HttpClient


/**
 * Provides methods for managing the KV version 2 secrets engine.
 * [Documentation](https://www.vaultproject.io/api-docs/secret/kv/kv-v2)
 */
public interface VaultKV2Engine {

}

/**
 * Implementation of [VaultKV2Engine].
 */
public class VaultKV2EngineImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultKV2Engine {

    public companion object {

        /**
         * Creates a new instance of [VaultKV2EngineImpl] using the provided HttpClient and optional parent path.
         * @param client HttpClient to interact with API.
         * @param parentPath The optional parent path used for building the final path used to interact with endpoints.
         * @param builder Builder to define the authentication service.
         * @return The instance of [VaultKV2EngineImpl] that was built.
         */
        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultKV2EngineImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "secret"
    }

    /**
     * Builder class to simplify the creation of [VaultKV2EngineImpl].
     */
    public class Builder : ServiceBuilder<VaultKV2EngineImpl>() {

        /**
         * The path to the KV mount to interact with, such as secret. This is specified as part of the URL.
         * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/kv/kv-v2)
         */
        public override var path: String = Default.PATH

        override fun buildWithFullPath(client: HttpClient, fullPath: String): VaultKV2EngineImpl = VaultKV2EngineImpl(
            client = client,
            path = fullPath
        )
    }

}
