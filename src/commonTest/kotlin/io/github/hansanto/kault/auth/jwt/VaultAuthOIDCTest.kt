package io.github.hansanto.kault.auth.jwt

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.jwt.common.OIDCResponseMode
import io.github.hansanto.kault.auth.jwt.common.OIDCResponseType
import io.github.hansanto.kault.auth.jwt.payload.OIDCCreateOrUpdatePayload
import io.github.hansanto.kault.auth.jwt.response.OIDCConfigureResponse
import io.github.hansanto.kault.auth.jwt.response.OIDCReadRoleResponse
import io.github.hansanto.kault.extension.toJsonPrimitiveMap
import io.github.hansanto.kault.util.DEFAULT_ROLE_NAME
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.enableAuthMethod
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.revokeAllOIDCData
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

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
                oidcDiscoveryUrl = "http://keycloak:8080/realms/vault"
                oidcClientId = "vault"
                oidcClientSecret = "vault-client-secret"
                defaultRole = "default-role"
                boundIssuer = "http://keycloak:8080/realms/vault"
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
            assertCreateRole(
                oidc,
                "cases/auth/oidc/create/without_options/given.json",
                "cases/auth/oidc/create/without_options/expected.json"
            )
        }

        should("create a role with all defined values") {
            assertCreateRole(
                oidc,
                "cases/auth/oidc/create/with_options/given.json",
                "cases/auth/oidc/create/with_options/expected.json"
            )
        }

    })

private suspend fun assertCreateRole(oidc: VaultAuthOIDC, givenPath: String, expectedReadPath: String) {
    assertCreateRole(
        oidc,
        givenPath,
        expectedReadPath
    ) { role, payload ->
        oidc.createOrUpdateRole(role, payload)
    }
}

private suspend fun assertCreateRoleWithBuilder(
    oidc: VaultAuthOIDC,
    givenPath: String,
    expectedReadPath: String
) {
    assertCreateRole(
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

private suspend inline fun assertCreateRole(
    oidc: VaultAuthOIDC,
    givenPath: String,
    expectedReadPath: String,
    createOrUpdate: (String, OIDCCreateOrUpdatePayload) -> Boolean
) {
    val given = readJson<OIDCCreateOrUpdatePayload>(givenPath)
    createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true
    oidc.readRole(DEFAULT_ROLE_NAME) shouldBe readJson<OIDCReadRoleResponse>(expectedReadPath)
}
