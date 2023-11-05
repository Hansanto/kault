package com.github.hansanto.kault

import io.ktor.client.HttpClient

public interface ServiceBuilder<T> {

    public var path: String

    public fun build(client: HttpClient, parentPath: String? = null): T
}
