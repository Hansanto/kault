package io.github.hansanto.kault.util

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.system.auth.enable
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging

const val ROOT_TOKEN = "root"

const val VAULT_URL = "http://localhost:8200"

inline fun createVaultClient(
    crossinline authBuilder: VaultClient.Builder.AuthBuilder.() -> Unit = {
        autoRenewToken = false
    }
): VaultClient = VaultClient {
    url = VAULT_URL
    auth {
        setTokenString(ROOT_TOKEN)
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

suspend fun enableAuthMethod(client: VaultClient, authMethod: String) {
    client.auth.setTokenString(ROOT_TOKEN)
    runCatching {
        client.system.auth.enable(authMethod) {
            type = authMethod
        }
    }
}

suspend fun revokeAllUserpassData(client: VaultClient) {
    client.auth.setTokenString(ROOT_TOKEN)
    val userpassService = client.auth.userpass

    runCatching { userpassService.list() }
        .onSuccess { users ->
            users.forEach { user ->
                userpassService.delete(user)
            }
        }
}

suspend fun revokeAllKubernetesData(client: VaultClient) {
    client.auth.setTokenString(ROOT_TOKEN)
    val kubernetesService = client.auth.kubernetes

    runCatching { kubernetesService.list() }
        .onSuccess { roles ->
            roles.forEach { role ->
                kubernetesService.deleteRole(role)
            }
        }
}

suspend fun revokeAllOIDCData(client: VaultClient) {
    client.auth.setTokenString(ROOT_TOKEN)
    val oidcService = client.auth.oidc

    runCatching { oidcService.list() }
        .onSuccess { roles ->
            roles.forEach { role ->
                oidcService.deleteRole(role)
            }
        }
}

suspend fun revokeAllAppRoleData(client: VaultClient) {
    client.auth.setTokenString(ROOT_TOKEN)
    val appRoleService = client.auth.appRole

    runCatching { appRoleService.list() }
        .onSuccess { roles ->
            roles.forEach { role ->
                appRoleService.delete(role)
            }
        }
}

suspend fun revokeAllTokenData(client: VaultClient) {
    client.auth.setTokenString(ROOT_TOKEN)
    val tokenService = client.auth.token
    val rootAccessor = tokenService.lookupSelfToken().accessor

    // Revoke all tokens from accessor except root accessor
    tokenService.listAccessors()
        .asSequence()
        .filter {
            it != rootAccessor
        }
        .forEach {
            tokenService.revokeAccessorToken(it)
        }

    // Revoke all token roles
    runCatching { tokenService.listTokenRoles() }
        .onSuccess { roles ->
            roles.forEach { role ->
                tokenService.deleteTokenRole(role)
            }
        }
}

suspend fun revokeEntity(client: VaultClient) {
    client.auth.setTokenString(ROOT_TOKEN)
    val identityEntity = client.identity.entity

    runCatching { identityEntity.listEntitiesByID() }
        .onSuccess { response ->
            identityEntity.batchDeleteEntities(response)
        }
}

suspend fun disableAllAudit(client: VaultClient) {
    client.auth.setTokenString(ROOT_TOKEN)
    val auditService = client.system.audit

    runCatching { auditService.list() }
        .onSuccess { audits ->
            audits.forEach { (key) ->
                auditService.disable(key)
            }
        }
}

suspend fun disableAllAuth(client: VaultClient) {
    client.auth.setTokenString(ROOT_TOKEN)
    val authService = client.system.auth

    runCatching { authService.list() }
        .onSuccess { auths ->
            auths
                .filterKeys { !it.contains("token") } // Cannot disable token auth
                .keys
                .forEach {
                    authService.disable(it)
                }
        }
}
