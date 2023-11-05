package com.github.hansanto.kault

import com.github.hansanto.kault.extension.addURLChildPath
import io.ktor.client.HttpClient

/**
 * ServiceBuilder is an interface that defines the contract for building a service instance.
 *
 * @param T the type of the service that will be built.
 * @property path the base path of the service.
 */
public abstract class ServiceBuilder<T> {

    public abstract var path: String

    /**
     * Builds an instance of type T using the provided HttpClient and optional parentPath.
     * The final path of the instance is built by concatenating the parentPath and the path of the instance.
     * If the parentPath is null, the final path is the same as the path of the instance.
     * If the parentPath is not null, the final path is the concatenation of the parentPath and the path of the instance.
     *
     * @param client The client to interact with API.
     * @param parentPath The optional parent path used for building the final path of the instance.
     * @return The instance of type T that was built.
     */
    public fun build(client: HttpClient, parentPath: String? = null): T = buildWithFullPath(client, parentPath?.addURLChildPath(path) ?: path)

    /**
     * Builds an instance of type T using the provided HttpClient and the concatenation of the parentPath and the path of the instance.
     * @param client The client to interact with API.
     * @param fullPath Concatenation of the parentPath and the [path] of the instance that should be used as a base path for the requests.
     * @return The instance of type T that was built.
     */
    protected abstract fun buildWithFullPath(client: HttpClient, fullPath: String): T
}

/**
 * A functional interface for constructing service instances using the builder pattern.
 *
 * @param T The type of the service instance being constructed.
 * @param B The type of the builder used to construct the service instance.
 */
public fun interface ServiceBuilderConstructor<T, B> {

    /**
     * Create a new instance of [T] using the builder pattern.
     * @param builder Builder to create the instance.
     * @return Instance of [T].
     */
    public operator fun invoke(client: HttpClient, parentPath: String?, builder: B.() -> Unit): T
}
