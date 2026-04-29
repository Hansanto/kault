package io.github.hansanto.kault.util

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.system.auth.enable
import io.ktor.client.HttpClient
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

const val ROOT_TOKEN = "root"

const val VAULT_URL = "http://localhost:8200"
const val VAULT_ENTERPRISE_URL = "http://localhost:8201"

/**
 * Maximum time to wait for a data has been processed by Vault in case of async operations, such as namespace deletion.
 */
val maxCheckVaultAsyncOpTimeout = 10.seconds

/**
 * Interval between checks for data processing completion in case of async operations, such as namespace deletion.
 */
val checkVaultAsyncOpInterval = 100.milliseconds

fun createVaultClient(
    authBuilder: VaultClient.Builder.AuthBuilder.() -> Unit = {
        autoRenewToken = false
    }
): VaultClient = createVaultClient(VAULT_URL, null, authBuilder)

fun createVaultEnterpriseClient(
    namespace: String? = null,
    authBuilder: VaultClient.Builder.AuthBuilder.() -> Unit = {
        autoRenewToken = false
    }
): VaultClient = createVaultClient(VAULT_ENTERPRISE_URL, namespace, authBuilder)

private fun createVaultClient(
    url: String,
    namespace: String? = null,
    authBuilder: VaultClient.Builder.AuthBuilder.() -> Unit = {
        autoRenewToken = false
    }
): VaultClient = VaultClient {
    this.url = url
    this.namespace = namespace
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
            identityEntity.batchDeleteEntities(response.keys)
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

suspend fun deleteAllNamespaces(client: VaultClient) {
    client.auth.setTokenString(ROOT_TOKEN)
    val namespacesService = client.system.namespaces

    runCatching { namespacesService.list() }
        .onSuccess { namespaces ->
            namespaces.keys.forEach {
                namespacesService.delete(it)
            }

            waitUntilVaultAsyncOpCompleted {
                val namespaces = runCatching { namespacesService.list() }
                    .getOrNull()
                    ?.keyInfo
                    // If the list() throws an exception, it means there are no namespaces, so we can consider the operation completed
                    ?: return@waitUntilVaultAsyncOpCompleted true

                // A tainted namespace means that the namespace is in the process of being deleted
                // If there are no tainted namespaces, it means that all namespaces have been deleted
                namespaces.asSequence().find { it.value.tainted } == null
            }
        }
}

suspend fun deleteAllPolicies(client: VaultClient) {
    client.auth.setTokenString(ROOT_TOKEN)
    val policyService = client.system.policy

    runCatching { policyService.list() }
        .onSuccess { policies ->
            policies
                .keys
                .filter { it != "default" && it != "root" }
                .forEach {
                    policyService.delete(it)
                }
        }
}

suspend fun deleteAllKV2Secrets(client: VaultClient) {
    client.auth.setTokenString(ROOT_TOKEN)
    val kv2 = client.secret.kv2

    val secrets = getAllSecrets(client)
    secrets.forEach { secret ->
        kv2.deleteMetadataAndAllVersions(secret)
    }
}

suspend fun getAllSecrets(client: VaultClient): Collection<String> = coroutineScope {
    val result = mutableSetOf<String>()
    val mutex = Mutex()
    val kv2 = client.secret.kv2

    suspend fun getSecretsRecursively(path: String) {
        val secrets = runCatching { kv2.listSecrets(path) }.getOrNull() ?: return

        secrets.map { secret ->
            async {
                if (secret.endsWith('/')) {
                    getSecretsRecursively(path + secret)
                } else {
                    val secretCompletePath = path + secret
                    mutex.withLock {
                        result.add(secretCompletePath)
                    }
                }
            }
        }.awaitAll()
    }

    getSecretsRecursively("")
    return@coroutineScope result
}

/**
 * Some API calls in Vault are asynchronous, such as namespace deletion.
 * This function allows to wait until the operation launched asynchronously is completed by periodically checking the condition provided in [isCompleted].
 *
 * @param isCompleted `true` if the operation is completed, `false` otherwise.
 */
suspend inline fun waitUntilVaultAsyncOpCompleted(crossinline isCompleted: suspend () -> Boolean) {
    withTimeout(maxCheckVaultAsyncOpTimeout) {
        while (isActive) {
            if (isCompleted()) {
                break
            }
            delay(checkVaultAsyncOpInterval)
        }
    }
}
