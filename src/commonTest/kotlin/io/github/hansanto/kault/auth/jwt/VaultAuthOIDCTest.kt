package io.github.hansanto.kault.auth.jwt

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.jwt.common.OIDCResponseMode
import io.github.hansanto.kault.auth.jwt.common.OIDCResponseType
import io.github.hansanto.kault.auth.jwt.common.OIDCRoleType
import io.github.hansanto.kault.auth.jwt.payload.OIDCCreateOrUpdatePayload
import io.github.hansanto.kault.auth.jwt.response.OIDCConfigureResponse
import io.github.hansanto.kault.auth.jwt.response.OIDCReadRoleResponse
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.extension.toJsonPrimitiveMap
import io.github.hansanto.kault.util.DEFAULT_ROLE_NAME
import io.github.hansanto.kault.util.KeycloakUtil
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.enableAuthMethod
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.revokeAllOIDCData
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import io.ktor.http.*

class VaultAuthOIDCTest :
    ShouldSpec({

        lateinit var client: VaultClient
        lateinit var oidc: VaultAuthOIDC

        beforeTest {
            client = createVaultClient()
            oidc = client.auth.oidc

            enableAuthMethod(client, "oidc")
            // TODO: Add keycloak as provider with https://developer.hashicorp.com/vault/api-docs/secret/identity/oidc-provider

            oidc.configure {
                oidcDiscoveryUrl = "${KeycloakUtil.HOST_FOR_VAULT}/realms/${KeycloakUtil.REALM}"
                oidcClientId = KeycloakUtil.CLIENT_ID
                oidcClientSecret = KeycloakUtil.CLIENT_SECRET
                defaultRole = "default-role"
                boundIssuer = "${KeycloakUtil.HOST_FOR_VAULT}/realms/${KeycloakUtil.REALM}"
            }
        }

        afterTest {
            revokeAllOIDCData(client)
            client.close()
        }

        should("use default path if not set in builder") {
            VaultAuthOIDCImpl.Default.PATH shouldBe "oidc"

            val built = VaultAuthOIDCImpl(client.client, null) {
            }

            built.path shouldBe VaultAuthOIDCImpl.Default.PATH
        }

        should("use custom path if set in builder") {
            val builderPath = randomString()
            val parentPath = randomString()

            val built = VaultAuthOIDCImpl(client.client, parentPath) {
                path = builderPath
            }

            built.path shouldBe "$parentPath/$builderPath"
        }

        should("read default configuration") {
            oidc.readConfiguration() shouldBe OIDCConfigureResponse(
                oidcDiscoveryUrl = "http://keycloak:8080/realms/vault",
                oidcClientId = "vault",
                defaultRole = "default-role",
                boundIssuer = "http://keycloak:8080/realms/vault",
                jwksCaPem = "",
                jwksPairs = emptyList(),
                jwksUrl = "",
                jwtSupportedAlgorithms = emptyList(),
                jwtValidationPubkeys = emptyList(),
                namespaceInState = true,
                oidcDiscoveryCaPem = "",
                oidcResponseMode = OIDCResponseMode.NONE,
                oidcResponseTypes = emptyList(),
                providerConfig = emptyMap()
            )
        }

        should("configure with jwks url") {
            oidc.configure {
                this.jwksUrl = "http://keycloak:8080/realms/vault/protocol/openid-connect/certs"
                this.jwtSupportedAlgorithms = listOf("RS256", "RS512")
                this.boundIssuer = "another-issuer"
                this.defaultRole = "another-role"
            }

            oidc.readConfiguration() shouldBe OIDCConfigureResponse(
                oidcDiscoveryUrl = "",
                oidcClientId = "",
                defaultRole = "another-role",
                boundIssuer = "another-issuer",
                jwksCaPem = "",
                jwksPairs = emptyList(),
                jwksUrl = "http://keycloak:8080/realms/vault/protocol/openid-connect/certs",
                jwtSupportedAlgorithms = listOf("RS256", "RS512"),
                jwtValidationPubkeys = emptyList(),
                namespaceInState = true,
                oidcDiscoveryCaPem = "",
                oidcResponseMode = OIDCResponseMode.NONE,
                oidcResponseTypes = emptyList(),
                providerConfig = emptyMap()
            )
        }

        should("configure with oidc full options") {
            oidc.configure {
                oidcDiscoveryUrl = "http://keycloak:8080/realms/vault"
                oidcClientId = "vault"
                oidcClientSecret = "vault-client-secret"
                defaultRole = "default-role"
                boundIssuer = "http://keycloak:8080/realms/vault"
                providerConfig = mapOf(
                    "provider" to "keycloak",
                )
                oidcResponseMode = OIDCResponseMode.FORM_POST
                oidcResponseTypes = listOf(OIDCResponseType.CODE, OIDCResponseType.ID_TOKEN)
            }

            oidc.readConfiguration() shouldBe OIDCConfigureResponse(
                oidcDiscoveryUrl = "http://keycloak:8080/realms/vault",
                oidcClientId = "vault",
                defaultRole = "default-role",
                boundIssuer = "http://keycloak:8080/realms/vault",
                jwksCaPem = "",
                jwksPairs = emptyList(),
                jwksUrl = "",
                jwtSupportedAlgorithms = emptyList(),
                jwtValidationPubkeys = emptyList(),
                namespaceInState = true,
                oidcDiscoveryCaPem = "",
                oidcResponseMode = OIDCResponseMode.FORM_POST,
                oidcResponseTypes = listOf(OIDCResponseType.CODE, OIDCResponseType.ID_TOKEN),
                providerConfig = mapOf(
                    "provider" to "gsuite",
                    "gsuite_service_account" to "/path/to/service-account.json",
                    "gsuite_admin_impersonate" to "admin@gsuitedomain.com",
                    "fetch_groups" to true,
                    "fetch_user_info" to true,
                    "groups_recurse_max_depth" to 5,
                    "user_custom_schemas" to "Education,Preferences",
                    "impersonate_principal" to "sa@project.iam.gserviceaccount.com"
                ).toJsonPrimitiveMap()
            )
        }

        should("create a role with default values") {
            assertCreateOrUpdateRole(
                oidc,
                "cases/auth/oidc/create/without_options/given.json",
                "cases/auth/oidc/create/without_options/expected.json"
            )
        }

        should("create a role with all defined values") {
            assertCreateOrUpdateRole(
                oidc,
                "cases/auth/oidc/create/with_options/given.json",
                "cases/auth/oidc/create/with_options/expected.json"
            )
        }

        should("create a role using builder with default values") {
            assertCreateOrUpdateRoleWithBuilder(
                oidc,
                "cases/auth/oidc/create/without_options/given.json",
                "cases/auth/oidc/create/without_options/expected.json"
            )
        }

        should("create a role using builder with all defined values") {
            assertCreateOrUpdateRoleWithBuilder(
                oidc,
                "cases/auth/oidc/create/with_options/given.json",
                "cases/auth/oidc/create/with_options/expected.json"
            )
        }

        should("throw exception if no role was created when listing roles") {
            shouldThrow<VaultAPIException> {
                oidc.list()
            }
        }

        should("return created roles when listing") {
            val roles = List(10) { "test-$it" }
            roles.forEach { createOIDCRole(oidc, it) }
            oidc.list() shouldContainExactlyInAnyOrder roles
        }

        should("do nothing when deleting non-existing role") {
            shouldThrow<VaultAPIException> { oidc.readRole(DEFAULT_ROLE_NAME) }
            oidc.deleteRole(DEFAULT_ROLE_NAME) shouldBe true
            shouldThrow<VaultAPIException> { oidc.readRole(DEFAULT_ROLE_NAME) }
        }

        should("delete existing role") {
            createOIDCRole(oidc, DEFAULT_ROLE_NAME)
            shouldNotThrow<VaultAPIException> { oidc.readRole(DEFAULT_ROLE_NAME) }
            oidc.deleteRole(DEFAULT_ROLE_NAME) shouldBe true
            shouldThrow<VaultAPIException> { oidc.readRole(DEFAULT_ROLE_NAME) }
        }

        should("throw exception if obtaining oidc authorization url with missing redirect uri") {
            val exception = shouldThrow<Exception> {
                oidc.oidcAuthorizationUrl {
                    this.role = DEFAULT_ROLE_NAME
                }
            }
            exception::class shouldNotBeSameInstanceAs VaultAPIException::class
        }

        should("throw exception if obtaining oidc authorization url when role does not exist") {
            shouldThrow<VaultAPIException> {
                oidc.oidcAuthorizationUrl {
                    this.role = DEFAULT_ROLE_NAME
                    this.redirectUri = "https://localhost:8080/callback"
                    this.clientNonce = randomString()
                }
            }
        }

        should("get oidc authorization url") {
            createOIDCRole(oidc, DEFAULT_ROLE_NAME)

            val clientNonce = randomString()
            val urlString = oidc.oidcAuthorizationUrl {
                this.redirectUri = "https://localhost:8080/callback"
                this.role = DEFAULT_ROLE_NAME
                this.clientNonce = clientNonce
            }

            val urlObject = Url(urlString)
            urlObject.protocol shouldBe URLProtocol.HTTP
            urlObject.host shouldBe "keycloak"
            urlObject.port shouldBe 8080
            urlObject.parameters["response_type"] shouldBe OIDCResponseType.CODE.value
            urlObject.parameters["client_id"] shouldBe "vault"
            urlObject.parameters["redirect_uri"] shouldBe "https://localhost:8080/callback"
            urlObject.parameters["scope"] shouldBe "openid"
            urlObject.parameters["nonce"] should {
                it != null && it.isNotEmpty()
            }
            urlObject.parameters["state"] should {
                it != null && it.isNotEmpty()
            }
        }

        should("throw exception when calling oidc callback without initiating auth flow") {
            createOIDCRole(oidc, DEFAULT_ROLE_NAME)

            shouldThrow<VaultAPIException> {
                oidc.oidcCallback {
                    state = "invalid-state"
                    nonce = "invalid-nonce"
                    code = "invalid-code"
                }
            }
        }

        should("throw exception when calling oidc callback with missing client nonce when required") {
            createOIDCRole(oidc, DEFAULT_ROLE_NAME)

            val clientNonce = randomString()
            val urlString = oidc.oidcAuthorizationUrl {
                this.redirectUri = "https://localhost:3000/callback"
                this.role = DEFAULT_ROLE_NAME
                this.clientNonce = clientNonce
            }

            val urlObject = Url(urlString)
            val state = urlObject.parameters["state"] ?: error("Missing state parameter")
            val nonce = urlObject.parameters["nonce"] ?: error("Missing nonce parameter")
            val ex = shouldThrow<VaultAPIException> {
                oidc.oidcCallback {
                    this.state = state
                    this.nonce = nonce
                    this.code = "invalid-code"
                }
            }

            ex.message shouldContain "invalid client_nonce"
        }

        should("call oidc callback successfully when providing correct parameters") {
            createOIDCRole(oidc, DEFAULT_ROLE_NAME)

            val clientNonce = randomString()
            val urlString = oidc.oidcAuthorizationUrl {
                this.redirectUri = "https://localhost:3000/callback"
                this.role = DEFAULT_ROLE_NAME
                this.clientNonce = clientNonce
            }

            val urlObject = Url(urlString)
            val state = urlObject.parameters["state"] ?: error("Missing state parameter")
            val nonce = urlObject.parameters["nonce"] ?: error("Missing nonce parameter")

            // Since we cannot complete the full OIDC flow in tests, we expect an exception due to invalid code
            val ex = shouldThrow<VaultAPIException> {
                oidc.oidcCallback {
                    this.state = state
                    this.nonce = nonce
                    this.code = "invalid-code"
                    this.clientNonce = clientNonce
                }
            }

            ex.message shouldContain "Code not valid"
        }

        should("throw exception when jwt login with invalid jwt token") {
            val (jwt, subject) = KeycloakUtil.getJwtWithPayload()
            configureJwt(oidc)
            createJwtOIDCRole(oidc, DEFAULT_ROLE_NAME, subject)

            val ex = shouldThrow<VaultAPIException> {
                oidc.jwtLogin {
                    this.jwt = jwt + "invalid-part"
                    this.role = DEFAULT_ROLE_NAME
                }
            }

            ex.message shouldContain "failed to verify id token signature"
        }

        should("throw exception when jwt login with role that does not exist") {
            val (jwt, subject) = KeycloakUtil.getJwtWithPayload()
            configureJwt(oidc)
            createJwtOIDCRole(oidc, DEFAULT_ROLE_NAME, subject)

            shouldThrow<VaultAPIException> {
                oidc.jwtLogin {
                    this.jwt = jwt
                    this.role = "non-existent-role"
                }
            }
        }

        should("throw exception when jwt login without default role configured") {
            val (jwt, subject) = KeycloakUtil.getJwtWithPayload()
            configureJwt(oidc)
            createJwtOIDCRole(oidc, DEFAULT_ROLE_NAME, subject)

            val ex = shouldThrow<VaultAPIException> {
                oidc.jwtLogin {
                    this.jwt = jwt
                    this.role = null
                }
            }

            ex.message shouldContain "missing role"
        }

        should("login using keycloak jwt token") {
            val (jwt, subject) = KeycloakUtil.getJwtWithPayload()
            configureJwt(oidc)
            createJwtOIDCRole(oidc, DEFAULT_ROLE_NAME, subject)
            shouldNotThrow<VaultAPIException> {
                oidc.jwtLogin {
                    this.jwt = jwt
                    this.role = DEFAULT_ROLE_NAME
                }
            }
        }
    })

private suspend fun configureJwt(oidc: VaultAuthOIDC) {
    oidc.configure {
        jwksUrl = KeycloakUtil.getJwksUrl()
        jwtSupportedAlgorithms = listOf("RS256")
    }
}

private suspend fun createOIDCRole(oidc: VaultAuthOIDC, role: String) {
    oidc.createOrUpdateRole(role) {
        userClaim = "sub"
        allowedRedirectUris = listOf("https://localhost:8080/callback")
    } shouldBe true
}

private suspend fun createJwtOIDCRole(oidc: VaultAuthOIDC, role: String, subject: String) {
    oidc.createOrUpdateRole(DEFAULT_ROLE_NAME) {
        roleType = OIDCRoleType.JWT
        userClaim = "sub"
        allowedRedirectUris = listOf("https://localhost:8080/callback")
        boundSubject = subject
    } shouldBe true
}

private suspend fun assertCreateOrUpdateRole(oidc: VaultAuthOIDC, givenPath: String, expectedReadPath: String) {
    assertCreateOrUpdateRole(
        oidc,
        givenPath,
        expectedReadPath
    ) { role, payload ->
        oidc.createOrUpdateRole(role, payload)
    }
}

private suspend fun assertCreateOrUpdateRoleWithBuilder(
    oidc: VaultAuthOIDC,
    givenPath: String,
    expectedReadPath: String
) {
    assertCreateOrUpdateRole(
        oidc,
        givenPath,
        expectedReadPath
    ) { role, payload ->
        oidc.createOrUpdateRole(role) {
            this.roleType = payload.roleType
            this.boundAudiences = payload.boundAudiences
            this.userClaim = payload.userClaim
            this.userClaimJsonPointer = payload.userClaimJsonPointer
            this.clockSkewLeeway = payload.clockSkewLeeway
            this.expirationLeeway = payload.expirationLeeway
            this.notBeforeLeeway = payload.notBeforeLeeway
            this.boundSubject = payload.boundSubject
            this.boundClaims = payload.boundClaims
            this.boundClaimsType = payload.boundClaimsType
            this.groupsClaim = payload.groupsClaim
            this.claimMappings = payload.claimMappings
            this.oidcScopes = payload.oidcScopes
            this.allowedRedirectUris = payload.allowedRedirectUris
            this.verboseOIDCLogging = payload.verboseOIDCLogging
            this.maxAge = payload.maxAge
            this.tokenTTL = payload.tokenTTL
            this.tokenMaxTTL = payload.tokenMaxTTL
            this.tokenPolicies = payload.tokenPolicies
            this.tokenBoundCidrs = payload.tokenBoundCidrs
            this.tokenExplicitMaxTTL = payload.tokenExplicitMaxTTL
            this.tokenNoDefaultPolicy = payload.tokenNoDefaultPolicy
            this.tokenNumUses = payload.tokenNumUses
            this.tokenPeriod = payload.tokenPeriod
            this.tokenType = payload.tokenType
        }
    }
}

private suspend inline fun assertCreateOrUpdateRole(
    oidc: VaultAuthOIDC,
    givenPath: String,
    expectedReadPath: String,
    createOrUpdate: (String, OIDCCreateOrUpdatePayload) -> Boolean
) {
    val given = readJson<OIDCCreateOrUpdatePayload>(givenPath)
    createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true
    oidc.readRole(DEFAULT_ROLE_NAME) shouldBe readJson<OIDCReadRoleResponse>(expectedReadPath)
}
