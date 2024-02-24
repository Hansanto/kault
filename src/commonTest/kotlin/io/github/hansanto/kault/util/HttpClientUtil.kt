package io.github.hansanto.kault.util

import io.github.hansanto.kault.VaultClient
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging

const val ROOT_TOKEN = "root"

fun createVaultClient(): VaultClient = VaultClient {
    url = "http://localhost:8200"
    auth {
        token = ROOT_TOKEN
    }
    httpClient { headers ->
        HttpClient {
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.INFO
            }
            defaultHttpClientConfiguration(headers)
        }
    }
}
