package io.github.hansanto.kault.util

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.VaultAuth
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging

const val ROOT_TOKEN = "root"

fun createVaultClient(
    authBuilder: VaultAuth.Builder.() -> Unit = {}
): VaultClient = VaultClient {
    url = "http://localhost:8200"
    auth {
        setToken(ROOT_TOKEN)
        authBuilder()
    }
    httpClient { headerBuilder ->
        HttpClient {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            defaultHttpClientConfiguration(headerBuilder)
        }
    }
}
