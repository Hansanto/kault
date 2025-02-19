package io.github.hansanto.kault.auth.token

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.token.payload.TokenCreatePayload
import io.github.hansanto.kault.auth.token.payload.TokenRenewAccessorPayload
import io.github.hansanto.kault.auth.token.payload.TokenRenewPayload
import io.github.hansanto.kault.auth.token.payload.TokenWriteRolePayload
import io.github.hansanto.kault.auth.token.response.TokenCreateResponse
import io.github.hansanto.kault.auth.token.response.TokenLookupResponse
import io.github.hansanto.kault.auth.token.response.TokenReadRoleResponse
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.serializer.VaultDuration
import io.github.hansanto.kault.util.ROOT_TOKEN
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.replaceTemplateString
import io.github.hansanto.kault.util.revokeAllTokenData
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

private const val TOKEN_ROLE_NAME = "test-role"

class VaultAuthTokenTest : ShouldSpec({

    lateinit var client: VaultClient
    lateinit var token: VaultAuthToken
    lateinit var rootAccessor: String

    beforeTest {
        client = createVaultClient()
        token = client.auth.token
        rootAccessor = token.lookupToken(ROOT_TOKEN).accessor
        revokeAllTokenData(client)
    }

    afterTest {
        client.close()
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

    should("create a token with all defined values") {
        assertCreateToken(
            token,
            "cases/auth/token/create/with_options/given.json",
            "cases/auth/token/create/with_options/expected.json"
        )
    }

    should("create a token using builder with default values") {
        assertCreateTokenWithBuilder(
            token,
            null,
            "cases/auth/token/create/without_options/expected.json"
        )
    }

    should("create a token using builder with all defined values") {
        assertCreateTokenWithBuilder(
            token,
            "cases/auth/token/create/with_options/given.json",
            "cases/auth/token/create/with_options/expected.json"
        )
    }

    should("create a token role name with default values") {
        assertCreateTokenRoleName(
            token,
            TOKEN_ROLE_NAME,
            null,
            "cases/auth/token/create_with_rolename/without_options/expected.json"
        )
    }

    should("create a token role name with all defined values") {
        assertCreateTokenRoleName(
            token,
            TOKEN_ROLE_NAME,
            "cases/auth/token/create_with_rolename/with_options/given.json",
            "cases/auth/token/create_with_rolename/with_options/expected.json"
        )
    }

    should("create a token role name using builder with default values") {
        assertCreateTokenRoleNameWithBuilder(
            token,
            TOKEN_ROLE_NAME,
            null,
            "cases/auth/token/create_with_rolename/without_options/expected.json"
        )
    }

    should("create a token with role name using builder with all defined values") {
        assertCreateTokenRoleNameWithBuilder(
            token,
            TOKEN_ROLE_NAME,
            "cases/auth/token/create_with_rolename/with_options/given.json",
            "cases/auth/token/create_with_rolename/with_options/expected.json"
        )
    }

    should("throw exception if lookup token with invalid token") {
        shouldThrow<VaultAPIException> {
            token.lookupToken("invalid-token")
        }
    }

    should("lookup a created token with default values") {
        assertLookupToken(
            token,
            null,
            "cases/auth/token/lookup/without_options/expected.json"
        )
    }

    should("lookup a created token with all defined values") {
        assertLookupToken(
            token,
            "cases/auth/token/lookup/with_options/given.json",
            "cases/auth/token/lookup/with_options/expected.json"
        )
    }

    should("lookup the self token with default values") {
        assertLookupSelfToken(
            client,
            null,
            "cases/auth/token/lookup_self/without_options/expected.json"
        )
    }

    should("lookup the self token with all defined values") {
        assertLookupSelfToken(
            client,
            "cases/auth/token/lookup_self/with_options/given.json",
            "cases/auth/token/lookup_self/with_options/expected.json"
        )
    }

    should("throw exception if lookup token with invalid accessor") {
        shouldThrow<VaultAPIException> {
            token.lookupAccessorToken("invalid-token")
        }
    }

    should("lookup token from accessor with default values") {
        assertLookupTokenFromAccessor(
            token,
            null,
            "cases/auth/token/lookup_accessor/without_options/expected.json"
        )
    }

    should("lookup token from accessor with all defined values") {
        assertLookupTokenFromAccessor(
            token,
            "cases/auth/token/lookup_accessor/with_options/given.json",
            "cases/auth/token/lookup_accessor/with_options/expected.json"
        )
    }

    should("throw exception if renew token with invalid token") {
        shouldThrow<VaultAPIException> {
            token.renewToken(TokenRenewPayload("invalid-token"))
        }
    }

    should("renew token with no increment") {
        assertRenewToken(
            token,
            null,
            "cases/auth/token/renew/without_options/expected.json"
        )
    }

    should("renew token with increment") {
        assertRenewToken(
            token,
            10.days,
            "cases/auth/token/renew/with_options/expected.json"
        )
    }

    should("renew token using builder with no increment") {
        assertRenewTokenWithBuilder(
            token,
            null,
            "cases/auth/token/renew/without_options/expected.json"
        )
    }

    should("renew token using builder with increment") {
        assertRenewTokenWithBuilder(
            token,
            10.days,
            "cases/auth/token/renew/with_options/expected.json"
        )
    }

    should("renew self token with no increment") {
        assertRenewSelfToken(
            client,
            null,
            "cases/auth/token/renew/without_options/expected.json"
        )
    }

    should("renew self token with increment") {
        assertRenewSelfToken(
            client,
            10.days,
            "cases/auth/token/renew/with_options/expected.json"
        )
    }

    should("renew token from accessor with no increment") {
        assertRenewTokenFromAccessor(
            token,
            null,
            "cases/auth/token/renew/without_options/expected.json"
        )
    }

    should("renew token from accessor with increment") {
        assertRenewTokenFromAccessor(
            token,
            10.days,
            "cases/auth/token/renew/with_options/expected.json"
        )
    }

    should("renew token from accessor using builder with no increment") {
        assertRenewTokenFromAccessorWithBuilder(
            token,
            null,
            "cases/auth/token/renew/without_options/expected.json"
        )
    }

    should("renew token from accessor using builder with increment") {
        assertRenewTokenFromAccessorWithBuilder(
            token,
            10.days,
            "cases/auth/token/renew/with_options/expected.json"
        )
    }

    should("do nothing if revoke token with invalid token") {
        val tokenValue = "invalid-token"
        shouldThrow<VaultAPIException> {
            token.lookupToken(tokenValue)
        }
        token.revokeToken(tokenValue) shouldBe true
        shouldThrow<VaultAPIException> {
            token.lookupToken(tokenValue)
        }
    }

    should("revoke existing token") {
        val response = token.createToken()
        token.revokeToken(response.clientToken) shouldBe true
        shouldThrow<VaultAPIException> {
            token.lookupToken(response.clientToken)
        }
    }

    should("revoke token and all children") {
        assertRevokeToken(client, false) {
            token.revokeToken(it.clientToken)
        }
    }

    should("revoke self token") {
        val response = token.createToken()
        client.auth.setTokenString(response.clientToken)
        token.revokeSelfToken() shouldBe true
        shouldThrow<VaultAPIException> {
            token.lookupToken(response.clientToken)
        }
    }

    should("revoke self token and all children") {
        assertRevokeToken(client, false) {
            token.revokeSelfToken()
        }
    }

    should("do nothing if revoke token with invalid accessor") {
        token.revokeAccessorToken("invalid-token") shouldBe true
    }

    should("revoke token from accessor") {
        val response = token.createToken()
        val accessor = response.accessor
        token.revokeAccessorToken(accessor) shouldBe true
        shouldThrow<VaultAPIException> {
            token.lookupToken(response.clientToken)
        }
    }

    should("revoke token from accessor and all children") {
        assertRevokeToken(client, false) {
            token.revokeAccessorToken(it.accessor)
        }
    }

    should("throw exception if revoke token and orphan children with invalid token") {
        shouldThrow<VaultAPIException> {
            token.revokeTokenAndOrphanChildren("invalid-token")
        }
    }

    should("revoke token and orphan children") {
        assertRevokeToken(client, true) {
            token.revokeTokenAndOrphanChildren(it.clientToken)
        }
    }

    should("throw exception if read token role with invalid token") {
        shouldThrow<VaultAPIException> {
            token.readTokenRole("invalid-token")
        }
    }

    should("throw exception when list token roles with no roles") {
        shouldThrow<VaultAPIException> {
            token.listTokenRoles()
        }
    }

    should("list token roles") {
        val roles = List(5) { "role-${it + 1}" }
        roles.forEach {
            token.createOrUpdateTokenRole(it)
        }
        val response = token.listTokenRoles()
        response shouldContainExactly roles
    }

    should("create and read a token role with default values") {
        assertCreateTokenRole(
            token,
            TOKEN_ROLE_NAME,
            null,
            "cases/auth/token/create_role/without_options/expected.json"
        )
    }

    should("create and read a token role with all defined values") {
        assertCreateTokenRole(
            token,
            TOKEN_ROLE_NAME,
            "cases/auth/token/create_role/with_options/given.json",
            "cases/auth/token/create_role/with_options/expected.json"
        )
    }

    should("create and read a token role using builder with default values") {
        assertCreateTokenRoleWithBuilder(
            token,
            TOKEN_ROLE_NAME,
            null,
            "cases/auth/token/create_role/without_options/expected.json"
        )
    }

    should("create and read a token role using builder with all defined values") {
        assertCreateTokenRoleWithBuilder(
            token,
            TOKEN_ROLE_NAME,
            "cases/auth/token/create_role/with_options/given.json",
            "cases/auth/token/create_role/with_options/expected.json"
        )
    }

    should("update and read a token role with default values") {
        assertUpdateTokenRole(
            token,
            TOKEN_ROLE_NAME,
            null,
            "cases/auth/token/create_role/without_options/expected.json"
        )
    }

    should("update and read a token role with all defined values") {
        assertUpdateTokenRole(
            token,
            TOKEN_ROLE_NAME,
            "cases/auth/token/create_role/with_options/given.json",
            "cases/auth/token/create_role/with_options/expected.json"
        )
    }

    should("update and read a token role using builder with default values") {
        assertUpdateTokenRoleWithBuilder(
            token,
            TOKEN_ROLE_NAME,
            null,
            "cases/auth/token/create_role/without_options/expected.json"
        )
    }

    should("update and read a token role using builder with all defined values") {
        assertUpdateTokenRoleWithBuilder(
            token,
            TOKEN_ROLE_NAME,
            "cases/auth/token/create_role/with_options/given.json",
            "cases/auth/token/create_role/with_options/expected.json"
        )
    }

    should("do nothing if delete token role with invalid role") {
        val role = randomString()
        token.deleteTokenRole(role) shouldBe true
        shouldThrow<VaultAPIException> {
            token.readTokenRole(role)
        }
    }

    should("delete token role") {
        val role = randomString()
        token.createOrUpdateTokenRole(role)

        token.deleteTokenRole(role) shouldBe true

        shouldThrow<VaultAPIException> {
            token.readTokenRole(role)
        }
    }

    should("tidy tokens should start internal task") {
        val warnings = token.tidyTokens()
        warnings.size shouldNotBe 0
    }
})

private suspend inline fun assertRevokeToken(
    client: VaultClient,
    childrenAreOrphan: Boolean,
    revoke: (TokenCreateResponse) -> Boolean
) {
    val token = client.auth.token

    val parentToken = token.createToken()
    client.auth.setTokenString(parentToken.clientToken)

    val orphans = List(5) {
        token.createToken {
            noParent = false
        }
    }

    orphans.forEach {
        it.orphan shouldBe false
    }

    revoke(parentToken) shouldBe true

    client.auth.setTokenString(ROOT_TOKEN)
    shouldThrow<VaultAPIException> {
        token.lookupToken(parentToken.clientToken)
    }

    orphans.forEach {
        if (childrenAreOrphan) {
            token.lookupToken(it.clientToken).orphan shouldBe true
        } else {
            shouldThrow<VaultAPIException> {
                token.lookupToken(it.clientToken)
            }
        }
    }
}

private suspend fun assertRenewToken(token: VaultAuthToken, increment: VaultDuration?, expectedReadPath: String) {
    assertRenewToken(token, increment, expectedReadPath) { _, tokenRenew ->
        token.renewToken(tokenRenew)
    }
}

private suspend fun assertRenewTokenWithBuilder(
    token: VaultAuthToken,
    increment: VaultDuration?,
    expectedReadPath: String
) {
    assertRenewToken(token, increment, expectedReadPath) { _, tokenRenew ->
        token.renewToken {
            this.token = tokenRenew.token
            this.increment = tokenRenew.increment
        }
    }
}

private suspend fun assertRenewSelfToken(client: VaultClient, increment: VaultDuration?, expectedReadPath: String) {
    val token = client.auth.token
    assertRenewToken(token, increment, expectedReadPath) { tokenCreate, tokenRenew ->
        client.auth.setTokenString(tokenCreate.clientToken)
        token.renewSelfToken(tokenRenew.increment)
    }
}

private suspend fun assertRenewTokenFromAccessor(
    token: VaultAuthToken,
    increment: VaultDuration?,
    expectedReadPath: String
) {
    assertRenewToken(token, increment, expectedReadPath) { tokenCreate, renew ->
        token.renewAccessorToken(TokenRenewAccessorPayload(tokenCreate.accessor, renew.increment))
    }
}

private suspend fun assertRenewTokenFromAccessorWithBuilder(
    token: VaultAuthToken,
    increment: VaultDuration?,
    expectedReadPath: String
) {
    assertRenewToken(token, increment, expectedReadPath) { tokenCreate, _ ->
        token.renewAccessorToken {
            this.accessor = tokenCreate.accessor
            this.increment = increment
        }
    }
}

private suspend inline fun assertRenewToken(
    token: VaultAuthToken,
    increment: VaultDuration?,
    expectedReadPath: String,
    renewToken: (TokenCreateResponse, TokenRenewPayload) -> TokenCreateResponse
) {
    val tokenCreateResponse = token.createToken {
        renewable = true
        ttl = 1.hours
    }

    val given = TokenRenewPayload(tokenCreateResponse.clientToken, increment)

    val renewTokenResponse = renewToken(tokenCreateResponse, given)
    val expected = readJson<TokenCreateResponse>(expectedReadPath)
    renewTokenResponse shouldBe replaceTemplateString(expected, renewTokenResponse)
}

private suspend fun assertLookupToken(token: VaultAuthToken, givenPath: String?, expectedReadPath: String) {
    assertLookupToken(token, givenPath, expectedReadPath) {
        token.lookupToken(it.clientToken)
    }
}

private suspend fun assertLookupTokenFromAccessor(token: VaultAuthToken, givenPath: String?, expectedReadPath: String) {
    assertLookupToken(token, givenPath, expectedReadPath) {
        token.lookupAccessorToken(it.accessor)
    }
}

private suspend fun assertLookupSelfToken(client: VaultClient, givenPath: String?, expectedReadPath: String) {
    val token = client.auth.token
    assertLookupToken(token, givenPath, expectedReadPath) {
        client.auth.setTokenString(it.clientToken)
        token.lookupSelfToken()
    }
}

private suspend inline fun assertLookupToken(
    token: VaultAuthToken,
    givenPath: String?,
    expectedReadPath: String,
    lookupToken: (TokenCreateResponse) -> TokenLookupResponse
) {
    val response = createToken(givenPath) { payload ->
        token.createToken(payload)
    }

    val lookupResponse = lookupToken(response)
    val expected = readJson<TokenLookupResponse>(expectedReadPath)
    lookupResponse shouldBe replaceTemplateString(expected, lookupResponse)
}

private suspend fun assertCreateToken(token: VaultAuthToken, givenPath: String?, expectedReadPath: String) {
    assertCreateToken(
        givenPath,
        expectedReadPath
    ) { payload ->
        if (givenPath != null) {
            token.createToken(payload)
        } else {
            token.createToken()
        }
    }
}

private suspend fun assertCreateTokenWithBuilder(token: VaultAuthToken, givenPath: String?, expectedReadPath: String) {
    assertCreateToken(
        givenPath,
        expectedReadPath
    ) { payload ->
        token.createToken {
            this.id = payload.id
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
    create: (TokenCreatePayload) -> TokenCreateResponse
) {
    val response = createToken(givenPath, create)

    val expected = readJson<TokenCreateResponse>(expectedReadPath)
    response shouldBe replaceTemplateString(expected, response)
}

private suspend fun assertCreateTokenRole(
    token: VaultAuthToken,
    roleName: String,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateTokenRole(
        token,
        roleName,
        givenPath,
        expectedReadPath
    ) { payload ->
        if (givenPath != null) {
            token.createOrUpdateTokenRole(roleName, payload)
        } else {
            token.createOrUpdateTokenRole(roleName)
        }
    }
}

private suspend fun assertCreateTokenRoleWithBuilder(
    token: VaultAuthToken,
    roleName: String,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateTokenRole(
        token,
        roleName,
        givenPath,
        expectedReadPath
    ) { payload ->
        token.createOrUpdateTokenRole(roleName) {
            this.allowedPolicies = payload.allowedPolicies
            this.disallowedPolicies = payload.disallowedPolicies
            this.allowedPoliciesGlob = payload.allowedPoliciesGlob
            this.disallowedPoliciesGlob = payload.disallowedPoliciesGlob
            this.orphan = payload.orphan
            this.renewable = payload.renewable
            this.pathSuffix = payload.pathSuffix
            this.allowedEntityAliases = payload.allowedEntityAliases
            this.tokenBoundCidrs = payload.tokenBoundCidrs
            this.tokenExplicitMaxTTL = payload.tokenExplicitMaxTTL
            this.tokenNoDefaultPolicy = payload.tokenNoDefaultPolicy
            this.tokenNumUses = payload.tokenNumUses
            this.tokenPeriod = payload.tokenPeriod
            this.tokenType = payload.tokenType
        }
    }
}

private suspend inline fun assertCreateTokenRole(
    token: VaultAuthToken,
    roleName: String,
    givenPath: String?,
    expectedReadPath: String,
    create: (TokenWriteRolePayload) -> Boolean
) {
    shouldThrow<VaultAPIException> {
        token.readTokenRole(roleName)
    }

    createTokenRole(givenPath, create) shouldBe true
    val roleConfiguration = token.readTokenRole(roleName)
    val expected = readJson<TokenReadRoleResponse>(expectedReadPath)
    roleConfiguration shouldBe replaceTemplateString(expected, roleConfiguration)
}

private suspend fun assertUpdateTokenRole(
    token: VaultAuthToken,
    roleName: String,
    givenPath: String?,
    expectedReadPath: String
) {
    assertUpdateTokenRole(
        token,
        roleName,
        givenPath,
        expectedReadPath
    ) { payload ->
        if (givenPath != null) {
            token.createOrUpdateTokenRole(roleName, payload)
        } else {
            token.createOrUpdateTokenRole(roleName)
        }
    }
}

private suspend fun assertUpdateTokenRoleWithBuilder(
    token: VaultAuthToken,
    roleName: String,
    givenPath: String?,
    expectedReadPath: String
) {
    assertUpdateTokenRole(
        token,
        roleName,
        givenPath,
        expectedReadPath
    ) { payload ->
        token.createOrUpdateTokenRole(roleName) {
            this.allowedPolicies = payload.allowedPolicies
            this.disallowedPolicies = payload.disallowedPolicies
            this.allowedPoliciesGlob = payload.allowedPoliciesGlob
            this.disallowedPoliciesGlob = payload.disallowedPoliciesGlob
            this.orphan = payload.orphan
            this.renewable = payload.renewable
            this.pathSuffix = payload.pathSuffix
            this.allowedEntityAliases = payload.allowedEntityAliases
            this.tokenBoundCidrs = payload.tokenBoundCidrs
            this.tokenExplicitMaxTTL = payload.tokenExplicitMaxTTL
            this.tokenNoDefaultPolicy = payload.tokenNoDefaultPolicy
            this.tokenNumUses = payload.tokenNumUses
            this.tokenPeriod = payload.tokenPeriod
            this.tokenType = payload.tokenType
        }
    }
}

private suspend inline fun assertUpdateTokenRole(
    token: VaultAuthToken,
    roleName: String,
    givenPath: String?,
    expectedReadPath: String,
    update: (TokenWriteRolePayload) -> Boolean
) {
    shouldThrow<VaultAPIException> {
        token.readTokenRole(roleName)
    }

    token.createOrUpdateTokenRole(roleName) shouldBe true
    createTokenRole(givenPath, update) shouldBe true

    val roleConfiguration = token.readTokenRole(roleName)
    val expected = readJson<TokenReadRoleResponse>(expectedReadPath)
    roleConfiguration shouldBe replaceTemplateString(expected, roleConfiguration)
}

private suspend fun assertCreateTokenRoleName(
    token: VaultAuthToken,
    roleName: String,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateTokenRoleName(
        token,
        roleName,
        givenPath,
        expectedReadPath
    ) { payload ->
        if (givenPath != null) {
            token.createToken(roleName, payload)
        } else {
            token.createToken(roleName)
        }
    }
}

private suspend fun assertCreateTokenRoleNameWithBuilder(
    token: VaultAuthToken,
    roleName: String,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateTokenRoleName(
        token,
        roleName,
        givenPath,
        expectedReadPath
    ) { payload ->
        token.createToken(roleName) {
            this.id = payload.id
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

private suspend inline fun assertCreateTokenRoleName(
    token: VaultAuthToken,
    roleName: String,
    givenPath: String?,
    expectedReadPath: String,
    create: (TokenCreatePayload) -> TokenCreateResponse
) {
    shouldThrow<VaultAPIException> {
        token.readTokenRole(roleName)
    }

    token.createOrUpdateTokenRole(roleName) shouldBe true
    val response = createToken(givenPath, create)

    val expected = readJson<TokenCreateResponse>(expectedReadPath)
    response shouldBe replaceTemplateString(expected, response)
}

private inline fun createToken(
    givenPath: String?,
    create: (TokenCreatePayload) -> TokenCreateResponse
): TokenCreateResponse {
    val given = givenPath?.let { readJson<TokenCreatePayload>(it) } ?: TokenCreatePayload()
    return create(given)
}

private inline fun createTokenRole(givenPath: String?, createOrUpdate: (TokenWriteRolePayload) -> Boolean): Boolean {
    val given = givenPath?.let { readJson<TokenWriteRolePayload>(it) } ?: TokenWriteRolePayload()
    return createOrUpdate(given)
}
