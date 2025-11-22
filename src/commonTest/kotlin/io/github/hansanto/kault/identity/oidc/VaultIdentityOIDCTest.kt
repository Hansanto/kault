package io.github.hansanto.kault.identity.oidc

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.identity.oidc.common.ClientType
import io.github.hansanto.kault.identity.oidc.payload.OIDCCreateOrUpdateAssignmentPayload
import io.github.hansanto.kault.identity.oidc.payload.OIDCCreateOrUpdateClientPayload
import io.github.hansanto.kault.identity.oidc.payload.OIDCCreateOrUpdateProviderPayload
import io.github.hansanto.kault.identity.oidc.payload.OIDCCreateOrUpdateScopePayload
import io.github.hansanto.kault.identity.oidc.response.OIDCListProvidersResponse
import io.github.hansanto.kault.identity.oidc.response.OIDCReadAssignmentResponse
import io.github.hansanto.kault.identity.oidc.response.OIDCReadClientResponse
import io.github.hansanto.kault.identity.oidc.response.OIDCReadProviderOpenIDConfigurationResponse
import io.github.hansanto.kault.identity.oidc.response.OIDCReadProviderResponse
import io.github.hansanto.kault.identity.oidc.response.OIDCReadScopeResponse
import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.util.DEFAULT_ROLE_NAME
import io.github.hansanto.kault.util.KeycloakUtil
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.revokeOIDCAssignments
import io.github.hansanto.kault.util.revokeOIDCClients
import io.github.hansanto.kault.util.revokeOIDCProviders
import io.github.hansanto.kault.util.revokeOIDCScopes
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlinx.serialization.Serializable

private const val DEFAULT_PROVIDER_NAME = "default"

private val DEFAULT_PROVIDER_INFO = createProviderResponse(
    name = DEFAULT_PROVIDER_NAME,
    allowedClientIds = listOf("*"),
    scopesSupported = emptyList()
)

class VaultIdentityOIDCTest : ShouldSpec({

    lateinit var client: VaultClient
    lateinit var oidc: VaultIdentityOIDC

    beforeTest {
        client = createVaultClient()
        oidc = client.identity.oidc
        // TODO: For createOrUpdateClient, try with a custom key: https://developer.hashicorp.com/vault/api-docs/secret/identity/tokens#create-a-named-key
    }

    afterTest {
        revokeOIDCProviders(client)
        revokeOIDCScopes(client)
        revokeOIDCClients(client)
        revokeOIDCAssignments(client)
        client.close()
    }

    should("use default path if not set in builder") {
        VaultIdentityOIDCImpl.Default.PATH shouldBe "oidc"

        val built = VaultIdentityOIDCImpl.Companion(client.client, null) {
        }

        built.path shouldBe VaultIdentityOIDCImpl.Default.PATH
    }

    should("use custom path if set in builder") {
        val builderPath = randomString()
        val parentPath = randomString()

        val built = VaultIdentityOIDCImpl.Companion(client.client, parentPath) {
            path = builderPath
        }

        built.path shouldBe "$parentPath/$builderPath"
    }

    should("create a provider with default values") {
        assertCreateProvider(
            oidc,
            null,
            "cases/identity/oidc/provider/create/without_options/expected.json"
        )
    }

    should("create a provider with all defined values") {
        oidc.createOrUpdateScope("test") {
            template = """{"test": "scope"}"""
        } shouldBe true

        assertCreateProvider(
            oidc,
            "cases/identity/oidc/provider/create/with_options/given.json",
            "cases/identity/oidc/provider/create/with_options/expected.json"
        )
    }

    should("create a provider using builder with default values") {
        assertCreateProviderWithBuilder(
            oidc,
            null,
            "cases/identity/oidc/provider/create/without_options/expected.json"
        )
    }

    should("create a provider using builder with all defined values") {
        oidc.createOrUpdateScope("test") {
            template = """{"test": "scope"}"""
        } shouldBe true

        assertCreateProviderWithBuilder(
            oidc,
            "cases/identity/oidc/provider/create/with_options/given.json",
            "cases/identity/oidc/provider/create/with_options/expected.json"
        )
    }

    should("update a provider if it exists") {
        oidc.createOrUpdateScope("test") {
            template = """{"test": "scope"}"""
        } shouldBe true

        oidc.createOrUpdateProvider(DEFAULT_ROLE_NAME) shouldBe true

        val given = readJson<OIDCCreateOrUpdateProviderPayload>("cases/identity/oidc/provider/update/given.json")
        oidc.createOrUpdateProvider(DEFAULT_ROLE_NAME, given) shouldBe true

        val expected = readJson<OIDCReadProviderResponse>("cases/identity/oidc/provider/update/expected.json")
        oidc.readProvider(DEFAULT_ROLE_NAME) shouldBe expected
    }

    should("throw exception when reading a non-existing provider") {
        shouldThrow<VaultAPIException> {
            oidc.readProvider("non-existing-provider")
        }
    }

    should("return provider info when reading an existing provider") {
        oidc.createOrUpdateProvider("test-0") shouldBe true

        oidc.readProvider("test-0") shouldBe createProviderResponse(
            "test-0"
        )
    }

    should("return only default providers when listing with no created providers") {
        oidc.listProviders() shouldBe OIDCListProvidersResponse(
            keyInfo = mapOf(
                DEFAULT_PROVIDER_NAME to DEFAULT_PROVIDER_INFO
            ),
            keys = listOf(DEFAULT_PROVIDER_NAME)
        )
    }

    should("return created providers when listing without filter") {
        oidc.createOrUpdateScope("test-scope") {
            template = """{"test": "scope"}"""
        } shouldBe true

        oidc.createOrUpdateProvider("test-0") shouldBe true
        oidc.createOrUpdateProvider("test-1") {
            allowedClientIds = listOf("client-1", "client-2")
            this.issuer = "https://example.com:8200"
            scopesSupported = listOf("test-scope")
        } shouldBe true

        assertListProviders(
            oidc.listProviders(),
            OIDCListProvidersResponse(
                keyInfo = mapOf(
                    DEFAULT_PROVIDER_NAME to DEFAULT_PROVIDER_INFO,
                    "test-0" to createProviderResponse("test-0"),
                    "test-1" to OIDCReadProviderResponse(
                        allowedClientIds = listOf("client-1", "client-2"),
                        issuer = "https://example.com:8200/v1/identity/oidc/provider/test-1",
                        scopesSupported = listOf("test-scope")
                    )
                ),
                keys = listOf(DEFAULT_PROVIDER_NAME, "test-0", "test-1")
            )
        )
    }

    should("return created providers when listing with filter") {
        oidc.createOrUpdateProvider("test-0") shouldBe true
        oidc.createOrUpdateProvider("test-1") {
            allowedClientIds = listOf("client-1", "client-2")
        } shouldBe true
        oidc.createOrUpdateProvider("test-2") {
            allowedClientIds = listOf("client-1", "client-3")
        }

        assertListProviders(
            oidc.listProviders("client-1"),
            OIDCListProvidersResponse(
                keyInfo = mapOf(
                    DEFAULT_PROVIDER_NAME to DEFAULT_PROVIDER_INFO,
                    "test-1" to OIDCReadProviderResponse(
                        allowedClientIds = listOf("client-1", "client-2"),
                        issuer = "http://0.0.0.0:8200/v1/identity/oidc/provider/test-1",
                        scopesSupported = emptyList()
                    ),
                    "test-2" to OIDCReadProviderResponse(
                        allowedClientIds = listOf("client-1", "client-3"),
                        issuer = "http://0.0.0.0:8200/v1/identity/oidc/provider/test-2",
                        scopesSupported = emptyList()
                    )
                ),
                keys = listOf(DEFAULT_PROVIDER_NAME, "test-1", "test-2")
            )
        )
    }

    should("return true when deleting a non-existing provider") {
        oidc.deleteProvider("test-0") shouldBe true

        shouldThrow<VaultAPIException> {
            oidc.readProvider("test-0")
        }
    }

    should("delete an existing provider") {
        oidc.createOrUpdateProvider("test-0") shouldBe true
        oidc.deleteProvider("test-0") shouldBe true
        shouldThrow<VaultAPIException> {
            oidc.readProvider("test-0")
        }
    }

    // Scopes

    should("create a scope with default values") {
        assertCreateScope(
            oidc,
            null,
            "cases/identity/oidc/scope/create/without_options/expected.json"
        )
    }

    should("create a scope with all defined values") {
        assertCreateScope(
            oidc,
            "cases/identity/oidc/scope/create/with_options/given.json",
            "cases/identity/oidc/scope/create/with_options/expected.json"
        )
    }

    should("create a scope using builder with default values") {
        assertCreateScopeWithBuilder(
            oidc,
            null,
            "cases/identity/oidc/scope/create/without_options/expected.json"
        )
    }

    should("create a scope using builder with all defined values") {
        assertCreateScopeWithBuilder(
            oidc,
            "cases/identity/oidc/scope/create/with_options/given.json",
            "cases/identity/oidc/scope/create/with_options/expected.json"
        )
    }

    should("create a scope with a typed template using builder") {
        @Serializable
        data class TestData(
            val name: String,
            val value: Int
        )

        val data = TestData("test", 42)

        oidc.createOrUpdateScope(DEFAULT_ROLE_NAME) {
            template(data)
        } shouldBe true

        oidc.readScope(DEFAULT_ROLE_NAME).template<TestData>() shouldBe data
    }

    should("update a scope if it exists") {
        oidc.createOrUpdateScope(DEFAULT_ROLE_NAME) shouldBe true

        val given = readJson<OIDCCreateOrUpdateScopePayload>("cases/identity/oidc/scope/update/given.json")
        oidc.createOrUpdateScope(DEFAULT_ROLE_NAME, given) shouldBe true

        val expected = readJson<OIDCReadScopeResponse>("cases/identity/oidc/scope/update/expected.json")
        oidc.readScope(DEFAULT_ROLE_NAME) shouldBe expected
    }

    should("throw exception when reading a non-existing scope") {
        shouldThrow<VaultAPIException> {
            oidc.readScope("non-existing-scope")
        }
    }

    should("return scope info when reading an existing scope") {
        oidc.createOrUpdateScope("test-0") shouldBe true

        oidc.readScope("test-0") shouldBe OIDCReadScopeResponse(
            template = "",
            description = ""
        )
    }

    should("throw exception when listing with no created scopes") {
        shouldThrow<VaultAPIException> {
            oidc.listScopes()
        }
    }

    should("return created scopes when listing scopes") {
        oidc.createOrUpdateScope("test-0") shouldBe true
        oidc.createOrUpdateScope("test-1") shouldBe true

        oidc.listScopes() shouldContainExactlyInAnyOrder listOf(
            "test-0",
            "test-1"
        )
    }

    should("return true when deleting a non-existing scope") {
        oidc.deleteScope("test-0") shouldBe true

        shouldThrow<VaultAPIException> {
            oidc.readScope("test-0")
        }
    }

    should("delete an existing scope") {
        oidc.createOrUpdateScope("test-0") shouldBe true
        oidc.deleteScope("test-0") shouldBe true
        shouldThrow<VaultAPIException> {
            oidc.readScope("test-0")
        }
    }

    // Clients

    should("create a client with default values") {
        assertCreateClient(
            oidc,
            null,
            "cases/identity/oidc/client/create/without_options/expected.json"
        )
    }

    should("create a client with all defined values for client type confidential") {
        assertCreateClient(
            oidc,
            "cases/identity/oidc/client/create/confidential/with_options/given.json",
            "cases/identity/oidc/client/create/confidential/with_options/expected.json"
        )
    }

    should("create a client with all defined values for client type public") {
        assertCreateClient(
            oidc,
            "cases/identity/oidc/client/create/public/with_options/given.json",
            "cases/identity/oidc/client/create/public/with_options/expected.json"
        )
    }

    should("create a client using builder with default values") {
        assertCreateClientWithBuilder(
            oidc,
            null,
            "cases/identity/oidc/client/create/without_options/expected.json"
        )
    }

    should("create a client using builder with all defined values for client type confidential") {
        assertCreateClientWithBuilder(
            oidc,
            "cases/identity/oidc/client/create/confidential/with_options/given.json",
            "cases/identity/oidc/client/create/confidential/with_options/expected.json"
        )
    }

    should("create a client using builder with all defined values for client type public") {
        assertCreateClientWithBuilder(
            oidc,
            "cases/identity/oidc/client/create/public/with_options/given.json",
            "cases/identity/oidc/client/create/public/with_options/expected.json"
        )
    }

    should("update a client if it exists") {
        oidc.createOrUpdateClient(DEFAULT_ROLE_NAME) shouldBe true

        val given = readJson<OIDCCreateOrUpdateClientPayload>("cases/identity/oidc/client/update/given.json")
        oidc.createOrUpdateClient(DEFAULT_ROLE_NAME, given) shouldBe true

        assertReadClientResponse(
            oidc.readClient(DEFAULT_ROLE_NAME),
            readJson<OIDCReadClientResponse>("cases/identity/oidc/client/update/expected.json")
        )
    }

    should("throw exception when reading a non-existing client") {
        shouldThrow<VaultAPIException> {
            oidc.readClient("non-existing-scope")
        }
    }

    should("return client info when reading an existing client") {
        oidc.createOrUpdateClient("test-0") shouldBe true

        assertReadClientResponse(
            oidc.readClient("test-0"),
            createReadClientResponse(ClientType.CONFIDENTIAL)
        )
    }

    should("throw exception when listing with no created clients") {
        shouldThrow<VaultAPIException> {
            oidc.listClients()
        }
    }

    should("return created clients when listing clients") {
        val expectedClient = List(10) {
            createReadClientResponse(
                clientType = if(it % 2 == 0) ClientType.CONFIDENTIAL else ClientType.PUBLIC,
                redirectUris = listOf("http://localhost:${it}"),
                idTokenTTL = it.seconds + 1.minutes,
                accessTokenTTL = it.seconds + 1.hours,
            )
        }

        expectedClient.forEachIndexed { i, client ->
            oidc.createOrUpdateClient("test-${i}") {
                this.key = client.key
                this.redirectUris = client.redirectUris
                this.assignments = client.assignments
                this.clientType = client.clientType
                this.idTokenTTL = client.idTokenTTL
                this.accessTokenTTL = client.accessTokenTTL
            } shouldBe true
        }

        val listClients = oidc.listClients()
        listClients.keys shouldContainExactlyInAnyOrder List(10) { "test-$it" }
        listClients.keyInfo shouldHaveSize 10

        expectedClient.forEachIndexed { i, client ->
            assertReadClientResponse(
                listClients.keyInfo["test-${i}"]!!,
                client
            )
        }
    }

    should("return true when deleting a non-existing client") {
        oidc.deleteClient("test-0") shouldBe true

        shouldThrow<VaultAPIException> {
            oidc.readClient("test-0")
        }
    }

    should("delete an existing client") {
        oidc.createOrUpdateClient("test-0") shouldBe true
        oidc.deleteClient("test-0") shouldBe true
        shouldThrow<VaultAPIException> {
            oidc.readClient("test-0")
        }
    }

    // Assignments

    should("create an assignment with default values") {
        assertCreateAssignment(
            oidc,
            null,
            "cases/identity/oidc/assignment/create/without_options/expected.json"
        )
    }

    should("create an assignment with all defined values") {
        assertCreateAssignment(
            oidc,
            "cases/identity/oidc/assignment/create/with_options/given.json",
            "cases/identity/oidc/assignment/create/with_options/expected.json"
        )
    }

    should("create an assignment using builder with default values") {
        assertCreateAssignmentWithBuilder(
            oidc,
            null,
            "cases/identity/oidc/assignment/create/without_options/expected.json"
        )
    }

    should("create an assignment using builder with all defined values") {
        assertCreateAssignmentWithBuilder(
            oidc,
            "cases/identity/oidc/assignment/create/with_options/given.json",
            "cases/identity/oidc/assignment/create/with_options/expected.json"
        )
    }

    should("update an assignment if it exists") {
        oidc.createOrUpdateAssignment(DEFAULT_ROLE_NAME) shouldBe true

        val given = readJson<OIDCCreateOrUpdateAssignmentPayload>("cases/identity/oidc/assignment/update/given.json")
        oidc.createOrUpdateAssignment(DEFAULT_ROLE_NAME, given) shouldBe true

        val expected = readJson<OIDCReadAssignmentResponse>("cases/identity/oidc/assignment/update/expected.json")
        oidc.readAssignment(DEFAULT_ROLE_NAME) shouldBe expected
    }

    should("throw exception when reading a non-existing assignment") {
        shouldThrow<VaultAPIException> {
            oidc.readAssignment("non-existing-assignments")
        }
    }

    should("return default assignment when listing with no created assignments") {
        oidc.listAssignments() shouldBe listOf("allow_all")
    }

    should("return created assignments when listing assignments") {
        oidc.createOrUpdateAssignment("test-0") shouldBe true
        oidc.createOrUpdateAssignment("test-1") shouldBe true

        oidc.listAssignments() shouldContainExactlyInAnyOrder listOf(
            "allow_all",
            "test-0",
            "test-1"
        )
    }

    should("return true when deleting a non-existing assignment") {
        oidc.deleteAssignment("test-0") shouldBe true

        shouldThrow<VaultAPIException> {
            oidc.readAssignment("test-0")
        }
    }

    should("delete an existing assignment") {
        oidc.createOrUpdateAssignment("test-0") shouldBe true
        oidc.deleteAssignment("test-0") shouldBe true
        shouldThrow<VaultAPIException> {
            oidc.readAssignment("test-0")
        }
    }

    should("throw exception when reading a non-existing provider openid configuration") {
        shouldThrow<VaultAPIException> {
            oidc.readProviderOpenIDConfiguration("non-existing-provider")
        }
    }

    should("return provider openid configuration when reading an existing provider") {
        // TODO wait for https://github.com/hashicorp/vault/issues/31655
        KeycloakUtil.createOrUpdateVaultOIDCProvider(oidc)

        val config = oidc.readProviderOpenIDConfiguration(KeycloakUtil.VAULT_PROVIDER_ID)
        val expected = readJson<OIDCReadProviderOpenIDConfigurationResponse>("cases/identity/oidc/openid/read/expected.json")
        config shouldBe expected
    }

    should("throw exception when reading a non-existing provider public keys") {
        shouldThrow<VaultAPIException> {
            oidc.readProviderPublicKeys("non-existing-provider")
        }
    }

    should("return provider public keys when reading an existing provider") {
        // TODO wait for https://github.com/hashicorp/vault/issues/31655
        KeycloakUtil.createOrUpdateVaultOIDCProvider(oidc)

        val keys = oidc.readProviderPublicKeys(KeycloakUtil.VAULT_PROVIDER_ID)
        keys shouldHaveSize 2
        keys.map { it.use } shouldContainExactlyInAnyOrder listOf("sig", "enc")
    }
})

private fun assertListProviders(
    response: OIDCListProvidersResponse,
    expected: OIDCListProvidersResponse
) {
    response.keys shouldContainExactlyInAnyOrder expected.keys
    response.keyInfo shouldContainExactly expected.keyInfo
}

private suspend fun assertCreateProvider(oidc: VaultIdentityOIDC, givenPath: String?, expectedReadPath: String) {
    assertCreateProvider(
        oidc,
        givenPath,
        expectedReadPath
    ) { providerName, payload ->
        oidc.createOrUpdateProvider(providerName, payload)
    }
}

private suspend fun assertCreateProviderWithBuilder(
    oidc: VaultIdentityOIDC,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateProvider(
        oidc,
        givenPath,
        expectedReadPath
    ) { providerName, payload ->
        oidc.createOrUpdateProvider(providerName) {
            this.issuer = payload.issuer
            this.allowedClientIds = payload.allowedClientIds
            this.scopesSupported = payload.scopesSupported
        }
    }
}

private suspend inline fun assertCreateProvider(
    oidc: VaultIdentityOIDC,
    givenPath: String?,
    expectedReadPath: String,
    createOrUpdate: (String, OIDCCreateOrUpdateProviderPayload) -> Boolean
) {
    val given =
        givenPath?.let { readJson<OIDCCreateOrUpdateProviderPayload>(it) } ?: OIDCCreateOrUpdateProviderPayload()
    createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true
    oidc.readProvider(DEFAULT_ROLE_NAME) shouldBe readJson<OIDCReadProviderResponse>(expectedReadPath)
}

private suspend fun assertCreateScope(oidc: VaultIdentityOIDC, givenPath: String?, expectedReadPath: String) {
    assertCreateScope(
        oidc,
        givenPath,
        expectedReadPath
    ) { providerName, payload ->
        oidc.createOrUpdateScope(providerName, payload)
    }
}

private suspend fun assertCreateScopeWithBuilder(
    oidc: VaultIdentityOIDC,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateScope(
        oidc,
        givenPath,
        expectedReadPath
    ) { providerName, payload ->
        oidc.createOrUpdateScope(providerName) {
            this.template = payload.template
            this.description = payload.description
        }
    }
}

private suspend inline fun assertCreateScope(
    oidc: VaultIdentityOIDC,
    givenPath: String?,
    expectedReadPath: String,
    createOrUpdate: (String, OIDCCreateOrUpdateScopePayload) -> Boolean
) {
    val given = givenPath?.let { readJson<OIDCCreateOrUpdateScopePayload>(it) } ?: OIDCCreateOrUpdateScopePayload()
    createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true
    oidc.readScope(DEFAULT_ROLE_NAME) shouldBe readJson<OIDCReadScopeResponse>(expectedReadPath)
}

private suspend fun assertCreateClient(oidc: VaultIdentityOIDC, givenPath: String?, expectedReadPath: String) {
    assertCreateClient(
        oidc,
        givenPath,
        expectedReadPath
    ) { providerName, payload ->
        oidc.createOrUpdateClient(providerName, payload)
    }
}

private suspend fun assertCreateClientWithBuilder(
    oidc: VaultIdentityOIDC,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateClient(
        oidc,
        givenPath,
        expectedReadPath
    ) { providerName, payload ->
        oidc.createOrUpdateClient(providerName) {
            this.key = payload.key
            this.redirectUris = payload.redirectUris
            this.assignments = payload.assignments
            this.clientType = payload.clientType
            this.idTokenTTL = payload.idTokenTTL
            this.accessTokenTTL = payload.accessTokenTTL
        }
    }
}

private suspend inline fun assertCreateClient(
    oidc: VaultIdentityOIDC,
    givenPath: String?,
    expectedReadPath: String,
    createOrUpdate: (String, OIDCCreateOrUpdateClientPayload) -> Boolean
) {
    val given = givenPath?.let { readJson<OIDCCreateOrUpdateClientPayload>(it) } ?: OIDCCreateOrUpdateClientPayload()
    createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true

    val value = oidc.readClient(DEFAULT_ROLE_NAME)
    val expected = readJson<OIDCReadClientResponse>(expectedReadPath)
    assertReadClientResponse(value, expected)
}

private suspend fun assertCreateAssignment(oidc: VaultIdentityOIDC, givenPath: String?, expectedReadPath: String) {
    assertCreateAssignment(
        oidc,
        givenPath,
        expectedReadPath
    ) { providerName, payload ->
        oidc.createOrUpdateAssignment(providerName, payload)
    }
}

private suspend fun assertCreateAssignmentWithBuilder(
    oidc: VaultIdentityOIDC,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateAssignment(
        oidc,
        givenPath,
        expectedReadPath
    ) { providerName, payload ->
        oidc.createOrUpdateAssignment(providerName) {
            this.entityIds = payload.entityIds
            this.groupIds = payload.groupIds
        }
    }
}

private suspend inline fun assertCreateAssignment(
    oidc: VaultIdentityOIDC,
    givenPath: String?,
    expectedReadPath: String,
    createOrUpdate: (String, OIDCCreateOrUpdateAssignmentPayload) -> Boolean
) {
    val given = givenPath?.let { readJson<OIDCCreateOrUpdateAssignmentPayload>(it) } ?: OIDCCreateOrUpdateAssignmentPayload()
    createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true

    val value = oidc.readAssignment(DEFAULT_ROLE_NAME)
    val expected = readJson<OIDCReadAssignmentResponse>(expectedReadPath)
    value shouldBe expected
}

private fun assertReadClientResponse(
    value: OIDCReadClientResponse,
    expected: OIDCReadClientResponse
) {
    expected.clientId = value.clientId
    if (expected.clientType == ClientType.CONFIDENTIAL) {
        expected.clientSecret = value.clientSecret
    }

    value shouldBe expected
}

private fun createReadClientResponse(
    clientType: ClientType,
    key: String = "default",
    redirectUris: List<String> = emptyList(),
    assignments: List<String> = emptyList(),
    idTokenTTL: VaultDuration = 86400.seconds,
    accessTokenTTL: VaultDuration = 86400.seconds
): OIDCReadClientResponse = OIDCReadClientResponse(
    key = key,
    redirectUris = redirectUris,
    assignments = assignments,
    clientId = "REPLACE",
    clientSecret = if (clientType == ClientType.CONFIDENTIAL) "REPLACE" else null,
    clientType = clientType,
    idTokenTTL = idTokenTTL,
    accessTokenTTL = accessTokenTTL
)

private fun createProviderResponse(
    name: String,
    allowedClientIds: List<String> = emptyList(),
    issuer: String = "http://0.0.0.0:8200/v1/identity/oidc/provider/$name",
    scopesSupported: List<String> = emptyList()
): OIDCReadProviderResponse = OIDCReadProviderResponse(
    allowedClientIds = allowedClientIds,
    issuer = issuer,
    scopesSupported = scopesSupported
)
