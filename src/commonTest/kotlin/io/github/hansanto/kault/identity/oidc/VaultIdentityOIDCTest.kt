package io.github.hansanto.kault.identity.oidc

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.identity.oidc.payload.OIDCCreateOrUpdateProviderPayload
import io.github.hansanto.kault.identity.oidc.payload.OIDCCreateOrUpdateScopePayload
import io.github.hansanto.kault.identity.oidc.response.OIDCListProvidersResponse
import io.github.hansanto.kault.identity.oidc.response.OIDCReadProviderResponse
import io.github.hansanto.kault.identity.oidc.response.OIDCReadScopeResponse
import io.github.hansanto.kault.util.DEFAULT_ROLE_NAME
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.revokeOIDCProviders
import io.github.hansanto.kault.util.revokeOIDCScopes
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.maps.shouldContainExactly
import io.kotest.matchers.shouldBe
import kotlin.collections.emptyList

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
    }

    afterTest {
        revokeOIDCProviders(client)
        revokeOIDCScopes(client)
        client.close()
    }

    xshould("use default path if not set in builder") {
        VaultIdentityOIDCImpl.Default.PATH shouldBe "oidc"

        val built = VaultIdentityOIDCImpl.Companion(client.client, null) {
        }

        built.path shouldBe VaultIdentityOIDCImpl.Default.PATH
    }

    xshould("use custom path if set in builder") {
        val builderPath = randomString()
        val parentPath = randomString()

        val built = VaultIdentityOIDCImpl.Companion(client.client, parentPath) {
            path = builderPath
        }

        built.path shouldBe "$parentPath/$builderPath"
    }

    xshould("create a provider with default values") {
        assertCreateProvider(
            oidc,
            null,
            "cases/identity/oidc/provider/create/without_options/expected.json"
        )
    }

    xshould("create a provider with all defined values") {
        oidc.createOrUpdateScope("test") shouldBe true
        assertCreateProvider(
            oidc,
            "cases/identity/oidc/provider/create/with_options/given.json",
            "cases/identity/oidc/provider/create/with_options/expected.json"
        )
    }

    xshould("create a provider using builder with default values") {
        assertCreateProviderWithBuilder(
            oidc,
            null,
            "cases/identity/oidc/provider/create/without_options/expected.json"
        )
    }

    xshould("create a provider using builder with all defined values") {
        oidc.createOrUpdateScope("test") shouldBe true
        assertCreateProviderWithBuilder(
            oidc,
            "cases/identity/oidc/provider/create/with_options/given.json",
            "cases/identity/oidc/provider/create/with_options/expected.json"
        )
    }

    xshould("update a provider if it exists") {
        oidc.createOrUpdateScope("test") shouldBe true
        oidc.createOrUpdateProvider(DEFAULT_ROLE_NAME) shouldBe true

        val given = readJson<OIDCCreateOrUpdateProviderPayload>("cases/identity/oidc/provider/update/given.json")
        oidc.createOrUpdateProvider(DEFAULT_ROLE_NAME, given) shouldBe true

        val expected = readJson<OIDCReadProviderResponse>("cases/identity/oidc/provider/update/expected.json")
        oidc.readProvider(DEFAULT_PROVIDER_NAME) shouldBe expected
    }

    xshould("throw exception when reading a non-existing provider") {
        shouldThrow<VaultAPIException> {
            oidc.readProvider("non-existing-provider")
        }
    }

    xshould("return provider info when reading an existing provider") {
        oidc.createOrUpdateProvider("test-0") shouldBe true

        oidc.readProvider("test-0") shouldBe createProviderResponse(
            "test-0"
        )
    }

    xshould("return only default providers when listing with no created providers") {
        oidc.listProviders() shouldBe OIDCListProvidersResponse(
            keyInfo = mapOf(
                DEFAULT_PROVIDER_NAME to DEFAULT_PROVIDER_INFO
            ),
            keys = listOf(DEFAULT_PROVIDER_NAME)
        )
    }

    xshould("return created providers when listing without filter") {
        oidc.createOrUpdateScope("test-scope") shouldBe true

        oidc.createOrUpdateProvider("test-0") shouldBe true
        oidc.createOrUpdateProvider("test-1") {
            allowedClientIds = listOf("client-1", "client-2")
            this.issuer = "https://example.com:8200"
            scopesSupported = listOf("test-scope")
        } shouldBe true

        oidc.listProviders() shouldBe OIDCListProvidersResponse(
            keyInfo = mapOf(
                DEFAULT_PROVIDER_NAME to DEFAULT_PROVIDER_INFO,
                "test-0" to OIDCReadProviderResponse(
                    allowedClientIds = listOf("*"),
                    issuer = "http://0.0.0.0:8200/v1/identity/oidc/provider/test-0",
                    scopesSupported = emptyList()
                ),
                "test-1" to OIDCReadProviderResponse(
                    allowedClientIds = listOf("client-1", "client-2"),
                    issuer = "https://example.com:8200",
                    scopesSupported = listOf("test-scope")
                )
            ),
            keys = listOf(DEFAULT_PROVIDER_NAME, "test-0", "test-1")
        )
    }

    xshould("return created providers when listing with filter") {
        oidc.createOrUpdateProvider("test-0") shouldBe true
        oidc.createOrUpdateProvider("test-1") {
            allowedClientIds = listOf("client-1", "client-2")
        } shouldBe true
        oidc.createOrUpdateProvider("test-2") {
            allowedClientIds = listOf("client-1", "client-3")
        }

        val response = oidc.listProviders("client-1")

        val expected = OIDCListProvidersResponse(
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

        response.keys shouldContainExactlyInAnyOrder expected.keys
        response.keyInfo shouldContainExactly expected.keyInfo
    }

    xshould("return true when deleting a non-existing provider") {
        oidc.deleteProvider("test-0") shouldBe true

        shouldThrow<VaultAPIException> {
            oidc.readProvider("test-0")
        }
    }

    xshould("delete an existing provider") {
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

})

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
    val given = givenPath?.let { readJson<OIDCCreateOrUpdateProviderPayload>(it) } ?: OIDCCreateOrUpdateProviderPayload()
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
