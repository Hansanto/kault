package com.github.hansanto.kault.util

import com.github.hansanto.kault.VaultClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging

fun createVaultClient(): VaultClient = VaultClient {
    url = "http://localhost:8200"
    auth {
        token = "root"
    }
    httpClient { tokenResolver, namespaceResolver ->
        HttpClient {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.ALL
            }
            defaultHttpClientConfiguration(tokenResolver, namespaceResolver)
        }
    }
}
