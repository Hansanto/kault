package io.github.hansanto.kault.auth.token

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.token.payload.TokenCreatePayload
import io.github.hansanto.kault.auth.token.response.TokenCreateResponse
import io.github.hansanto.kault.util.ROOT_TOKEN
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.replaceTemplateString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class VaultAuthTokenTest : ShouldSpec({

    lateinit var client: VaultClient
    lateinit var token: VaultAuthToken
    lateinit var rootAccessor: String

    beforeSpec {
        client = createVaultClient()
        token = client.auth.token
        rootAccessor = token.lookupToken(ROOT_TOKEN).accessor
    }

    afterSpec {
        client.close()
    }

    beforeTest {
        token.listAccessors()
            .asSequence()
            .filter {
                it != rootAccessor
            }
            .forEach {
                token.revokeAccessorToken(it)
            }
    }

    should("use default path if not set in builder") {
        VaultAuthTokenImpl.Default.PATH shouldBe "token"

        val built = VaultAuthTokenImpl(client.client, null) {
        }

        built.path shouldBe VaultAuthTokenImpl.Default.PATH
    }

    should("use custom path if set in builder") {
        val builderPath = randomString()
        val parentPath = randomString()

        val built = VaultAuthTokenImpl(client.client, parentPath) {
            path = builderPath
        }

        built.path shouldBe "$parentPath/$builderPath"
    }

    should("return only root accessor") {
        token.listAccessors() shouldContainExactly listOf(rootAccessor)
    }

    should("return all accessors") {
        val accessors = List(5) { token.createToken().accessor } + rootAccessor
        token.listAccessors() shouldContainExactlyInAnyOrder accessors
    }

    should("create a token with default values") {
        assertCreateToken(
            token,
            null,
            "cases/auth/token/create/without_options/expected.json"
        )
    }

    should("create a role with all defined values") {
        assertCreateToken(
            token,
            "cases/auth/token/create/with_options/given.json",
            "cases/auth/token/create/with_options/expected.json"
        )
    }
})

private suspend fun assertCreateToken(
    token: VaultAuthToken,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateToken(
        givenPath,
        expectedReadPath
    ) { payload ->
        token.createToken(payload)
    }
}

private suspend fun assertCreateTokenWithBuilder(
    token: VaultAuthToken,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateToken(
        givenPath,
        expectedReadPath
    ) { payload ->
        token.createOrUpdate {
            this.id = payload.id
            this.roleName = payload.roleName
            this.policies = payload.policies
            this.metadata = payload.metadata
            this.noParent = payload.noParent
            this.noDefaultPolicy = payload.noDefaultPolicy
            this.renewable = payload.renewable
            this.ttl = payload.ttl
            this.type = payload.type
            this.explicitMaxTTL = payload.explicitMaxTTL
            this.displayName = payload.displayName
            this.numUses = payload.numUses
            this.period = payload.period
            this.entityAlias = payload.entityAlias
        }
    }
}

private inline fun assertCreateToken(
    givenPath: String?,
    expectedReadPath: String,
    createOrUpdate: (TokenCreatePayload) -> TokenCreateResponse
) {
    val given = givenPath?.let { readJson<TokenCreatePayload>(it) } ?: TokenCreatePayload()
    val expected = readJson<TokenCreateResponse>(expectedReadPath)
    val response = createOrUpdate(given)

    response shouldBe replaceTemplateString(expected, response)
}