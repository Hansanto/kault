package com.github.hansanto.kault

import io.ktor.client.HttpClient

/**
 * ServiceBuilder is an interface that defines the contract for building a service instance.
 *
 * @param T the type of the service that will be built.
 * @property path the base path of the service.
 */
public interface ServiceBuilder<T> {

    public var path: String

    /**
     * Builds an instance of type T using the provided HttpClient and optional parentPath.
     *
     * @param client The HttpClient used for building the instance.
     * @param parentPath The optional parent path used for building the final path of the instance.
     * @return The instance of type T that was built.
     */
    public fun build(client: HttpClient, parentPath: String? = null): T

}
