package io.github.hansanto.kault.auth.approle

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.approle.payload.AuthAppRoleCreateCustomSecretIdPayload
import io.github.hansanto.kault.auth.approle.payload.AuthAppRoleCreateOrUpdatePayload
import io.github.hansanto.kault.auth.approle.payload.AuthAppRoleGenerateSecretIdPayload
import io.github.hansanto.kault.auth.approle.payload.AuthAppRoleLoginPayload
import io.github.hansanto.kault.auth.approle.response.AuthAppRoleGenerateSecretIdResponse
import io.github.hansanto.kault.auth.approle.response.AuthAppRoleReadResponse
import io.github.hansanto.kault.auth.approle.response.AuthAppRoleReadRoleIdResponse
import io.github.hansanto.kault.auth.approle.response.AuthAppRoleReadSecretIdResponse
import io.github.hansanto.kault.auth.common.response.LoginResponse
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.util.DEFAULT_ROLE_NAME
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.enableAuthMethod
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.replaceTemplateString
import io.github.hansanto.kault.util.revokeAllAppRoleData
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class VaultAuthAppRoleTest :
    ShouldSpec({

        lateinit var client: VaultClient
        lateinit var appRole: VaultAuthAppRole

        beforeTest {
            client = createVaultClient()
            appRole = client.auth.appRole

            enableAuthMethod(client, "approle")
            shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        }

        afterTest {
            revokeAllAppRoleData(client)
            client.close()
        }

        should("use default path if not set in builder") {
            VaultAuthAppRoleImpl.Default.PATH shouldBe "approle"

            val built = VaultAuthAppRoleImpl(client.client, null) {
            }

            built.path shouldBe VaultAuthAppRoleImpl.Default.PATH
        }

        should("use custom path if set in builder") {
            val builderPath = randomString()
            val parentPath = randomString()

            val built = VaultAuthAppRoleImpl(client.client, parentPath) {
                path = builderPath
            }

            built.path shouldBe "$parentPath/$builderPath"
        }

        should("throw exception if no role was created when listing roles") {
            shouldThrow<VaultAPIException> {
                appRole.list()
            }
        }

        should("return created roles when listing") {
            val roles = List(10) { "test-$it" }
            roles.forEach { appRole.createOrUpdate(it) shouldBe true }
            appRole.list() shouldBe roles
        }

        should("create a role with default values") {
            assertCreateRole(
                appRole,
                null,
                "cases/auth/approle/create/without_options/expected.json"
            )
        }

        should("create a role with all defined values") {
            assertCreateRole(
                appRole,
                "cases/auth/approle/create/with_options/given.json",
                "cases/auth/approle/create/with_options/expected.json"
            )
        }

        should("create a role using builder with default values") {
            assertCreateRoleWithBuilder(
                appRole,
                null,
                "cases/auth/approle/create/without_options/expected.json"
            )
        }

        should("create a role using builder with all defined values") {
            assertCreateRoleWithBuilder(
                appRole,
                "cases/auth/approle/create/with_options/given.json",
                "cases/auth/approle/create/with_options/expected.json"
            )
        }

        should("update a role if it exists") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

            val given = readJson<AuthAppRoleCreateOrUpdatePayload>("cases/auth/approle/update/given.json")
            appRole.createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true

            val expected = readJson<AuthAppRoleReadResponse>("cases/auth/approle/update/expected.json")
            appRole.read(DEFAULT_ROLE_NAME) shouldBe expected
        }

        should("throw exception when reading non-existing role") {
            shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        }

        should("read existing role") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            val response = appRole.read(DEFAULT_ROLE_NAME)
            response shouldNotBe null
        }

        should("do nothing when deleting non-existing role") {
            shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
            appRole.delete(DEFAULT_ROLE_NAME) shouldBe true
            shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        }

        should("delete existing role") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            shouldNotThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
            appRole.delete(DEFAULT_ROLE_NAME) shouldBe true
            shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        }

        should("throw exception when reading non-existing roleId") {
            shouldThrow<VaultAPIException> { appRole.readRoleId(DEFAULT_ROLE_NAME) }
        }

        should("read roleId with existing role") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            val response = appRole.readRoleId(DEFAULT_ROLE_NAME)
            response.roleId.length shouldNotBe 0
        }

        should("throw exception when updating roleId with non-existing role") {
            shouldThrow<VaultAPIException> { appRole.updateRoleId(DEFAULT_ROLE_NAME, "test") }
        }

        should("update roleId with existing role") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

            val newRoleId = "test"
            val previousRoleId = appRole.readRoleId(DEFAULT_ROLE_NAME)
            previousRoleId.roleId shouldNotBe newRoleId

            val expectedResponse = AuthAppRoleReadRoleIdResponse(newRoleId)
            appRole.updateRoleId(DEFAULT_ROLE_NAME, newRoleId) shouldBe true
            appRole.readRoleId(DEFAULT_ROLE_NAME) shouldBe expectedResponse
        }

        should("throw exception when generating secret id with non-existing role") {
            shouldThrow<VaultAPIException> { appRole.generateSecretId(DEFAULT_ROLE_NAME) }
        }

        should("generate secret id with existing role with default values") {
            assertGenerateSecretId(
                appRole,
                null,
                "cases/auth/approle/generate_secret_id/without_options/expected_write.json",
                "cases/auth/approle/generate_secret_id/without_options/expected_read.json"
            )
        }

        should("generate secret id using builder with existing role with default values") {
            assertGenerateSecretIdWithBuilder(
                appRole,
                null,
                "cases/auth/approle/generate_secret_id/without_options/expected_write.json",
                "cases/auth/approle/generate_secret_id/without_options/expected_read.json"
            )
        }

        should("generate secret id with existing role with all defined values") {
            assertGenerateSecretId(
                appRole,
                "cases/auth/approle/generate_secret_id/with_options/given.json",
                "cases/auth/approle/generate_secret_id/with_options/expected_write.json",
                "cases/auth/approle/generate_secret_id/with_options/expected_read.json"
            )
        }

        should("generate secret id using builder with existing role with all defined values") {
            assertGenerateSecretIdWithBuilder(
                appRole,
                "cases/auth/approle/generate_secret_id/with_options/given.json",
                "cases/auth/approle/generate_secret_id/with_options/expected_write.json",
                "cases/auth/approle/generate_secret_id/with_options/expected_read.json"
            )
        }

        should("throw exception when get secret id accessors with non-existing role") {
            shouldThrow<VaultAPIException> { appRole.secretIdAccessors(DEFAULT_ROLE_NAME) }
        }

        should("throw exception when get secret id accessors with existing role without secret id") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

            shouldThrow<VaultAPIException> { appRole.secretIdAccessors(DEFAULT_ROLE_NAME) }
        }

        should("generate secret id accessors with existing role") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

            repeat(10) {
                appRole.generateSecretId(DEFAULT_ROLE_NAME)
                appRole.secretIdAccessors(DEFAULT_ROLE_NAME).size shouldBe it + 1
            }
        }

        should("throw exception when read secret id with non-existing role") {
            shouldThrow<VaultAPIException> { appRole.readSecretId(DEFAULT_ROLE_NAME, "test") }
        }

        should("return null when read secret id with existing role and non-existing secret id") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            appRole.readSecretId(DEFAULT_ROLE_NAME, "test") shouldBe null
        }

        should("read secret id with existing role and existing secret id") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            val generateResponse = appRole.generateSecretId(DEFAULT_ROLE_NAME)
            appRole.readSecretId(DEFAULT_ROLE_NAME, generateResponse.secretId) shouldNotBe null
        }

        should("throw exception when destroy secret id with non-existing role") {
            shouldThrow<VaultAPIException> { appRole.destroySecretId(DEFAULT_ROLE_NAME, "test") }
        }

        should("destroy secret id with existing role and non-existing secret id") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            appRole.destroySecretId(DEFAULT_ROLE_NAME, "test") shouldBe true
        }

        should("destroy secret id with existing role and existing secret id") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            val secretId = appRole.generateSecretId(DEFAULT_ROLE_NAME).secretId

            appRole.readSecretId(DEFAULT_ROLE_NAME, secretId) shouldNotBe null
            appRole.destroySecretId(DEFAULT_ROLE_NAME, secretId) shouldBe true
            appRole.readSecretId(DEFAULT_ROLE_NAME, secretId) shouldBe null
        }

        should("throw exception when read secret id accessor with non-existing role") {
            shouldThrow<VaultAPIException> { appRole.readSecretIdAccessor(DEFAULT_ROLE_NAME, "test") }
        }

        should("read secret id accessor with existing role and non-existing secret id") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            shouldThrow<VaultAPIException> { appRole.readSecretIdAccessor(DEFAULT_ROLE_NAME, "test") }
        }

        should("read secret id accessor with existing role and existing secret id with default values") {
            assertReadSecretIdAccessor(
                appRole,
                null,
                "cases/auth/approle/read_secret_id_accessor/without_options/expected_write.json",
                "cases/auth/approle/read_secret_id_accessor/without_options/expected_read.json"
            )
        }

        should("read secret id accessor with existing role and existing secret id with all defined values") {
            assertReadSecretIdAccessor(
                appRole,
                "cases/auth/approle/read_secret_id_accessor/with_options/given.json",
                "cases/auth/approle/read_secret_id_accessor/with_options/expected_write.json",
                "cases/auth/approle/read_secret_id_accessor/with_options/expected_read.json"
            )
        }

        should("throw exception when destroy secret id accessor with non-existing role") {
            shouldThrow<VaultAPIException> { appRole.destroySecretIdAccessor(DEFAULT_ROLE_NAME, "test") }
        }

        should("throw exception when destroy secret id accessor with existing role and non-existing secret id") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            shouldThrow<VaultAPIException> { appRole.destroySecretIdAccessor(DEFAULT_ROLE_NAME, "test") }
        }

        should("destroy secret id accessor with existing role and existing secret id") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            val secretIdAccessor = appRole.generateSecretId(DEFAULT_ROLE_NAME).secretIdAccessor

            shouldNotThrow<VaultAPIException> { appRole.readSecretIdAccessor(DEFAULT_ROLE_NAME, secretIdAccessor) }
            appRole.destroySecretIdAccessor(DEFAULT_ROLE_NAME, secretIdAccessor) shouldBe true
            shouldThrow<VaultAPIException> { appRole.readSecretIdAccessor(DEFAULT_ROLE_NAME, secretIdAccessor) }
        }

        should("throw exception when create custom secret id with non-existing role") {
            shouldThrow<VaultAPIException> {
                appRole.createCustomSecretId(
                    DEFAULT_ROLE_NAME,
                    AuthAppRoleCreateCustomSecretIdPayload(randomString())
                )
            }
        }

        should("create custom secret id with existing role with empty secret-id") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            shouldThrow<VaultAPIException> {
                appRole.createCustomSecretId(
                    DEFAULT_ROLE_NAME,
                    AuthAppRoleCreateCustomSecretIdPayload("")
                )
            }
        }

        should("create custom secret id with existing role with default values with default values") {
            assertCreateCustomSecretId(
                appRole,
                "cases/auth/approle/create_custom_secret_id/without_options/given.json",
                "cases/auth/approle/create_custom_secret_id/without_options/expected_write.json",
                "cases/auth/approle/create_custom_secret_id/without_options/expected_read.json"
            )
        }

        should("create custom secret id using builder with existing role with default values with default values") {
            assertCreateCustomSecretIdWithBuilder(
                appRole,
                "cases/auth/approle/create_custom_secret_id/without_options/given.json",
                "cases/auth/approle/create_custom_secret_id/without_options/expected_write.json",
                "cases/auth/approle/create_custom_secret_id/without_options/expected_read.json"
            )
        }

        should("create custom secret id with existing role with all defined values") {
            assertCreateCustomSecretId(
                appRole,
                "cases/auth/approle/create_custom_secret_id/with_options/given.json",
                "cases/auth/approle/create_custom_secret_id/with_options/expected_write.json",
                "cases/auth/approle/create_custom_secret_id/with_options/expected_read.json"
            )
        }

        should("create custom secret id using builder with existing role with all defined values") {
            assertCreateCustomSecretIdWithBuilder(
                appRole,
                "cases/auth/approle/create_custom_secret_id/with_options/given.json",
                "cases/auth/approle/create_custom_secret_id/with_options/expected_write.json",
                "cases/auth/approle/create_custom_secret_id/with_options/expected_read.json"
            )
        }

        should("throw exception when login with non-existing role") {
            shouldThrow<VaultAPIException> { appRole.login(AuthAppRoleLoginPayload(DEFAULT_ROLE_NAME, randomString())) }
        }

        should("throw exception when login with existing role with empty secret-id") {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            val roleId = appRole.readRoleId(DEFAULT_ROLE_NAME).roleId
            shouldThrow<VaultAPIException> { appRole.login(AuthAppRoleLoginPayload(roleId, "")) }
        }

        should("login with existing role with secret-id") {
            assertLogin(
                appRole,
                "cases/auth/approle/login/expected.json"
            )
        }

        should("login using builder with existing role with secret-id") {
            assertLoginWithBuilder(
                appRole,
                "cases/auth/approle/login/expected.json"
            )
        }

        should("tidy tokens should start internal task") {
            val warnings = appRole.tidyTokens()
            warnings.size shouldNotBe 0
        }
    })

private suspend fun assertGenerateSecretId(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedWritePath: String,
    expectedReadPath: String
) {
    assertCreateAndReadSecret(
        appRole,
        givenPath,
        expectedWritePath,
        expectedReadPath,
        defaultPayload = { AuthAppRoleGenerateSecretIdPayload() },
        write = { role, payload ->
            appRole.generateSecretId(role, payload)
        },
        read = { role, response ->
            appRole.readSecretId(role, response.secretId)!!
        }
    )
}

private suspend fun assertGenerateSecretIdWithBuilder(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedWritePath: String,
    expectedReadPath: String
) {
    assertCreateAndReadSecret(
        appRole,
        givenPath,
        expectedWritePath,
        expectedReadPath,
        defaultPayload = { AuthAppRoleGenerateSecretIdPayload() },
        write = { role, payload ->
            appRole.generateSecretId(role) {
                this.metadata = payload.metadata
                this.cidrList = payload.cidrList
                this.tokenBoundCidrs = payload.tokenBoundCidrs
                this.numUses = payload.numUses
                this.ttl = payload.ttl
            }
        },
        read = { role, response ->
            appRole.readSecretId(role, response.secretId)!!
        }
    )
}

private suspend fun assertCreateCustomSecretId(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedWritePath: String,
    expectedReadPath: String
) {
    assertCreateAndReadSecret<AuthAppRoleCreateCustomSecretIdPayload>(
        appRole,
        givenPath,
        expectedWritePath,
        expectedReadPath,
        defaultPayload = { error("Should not be called") },
        write = { role, payload ->
            appRole.createCustomSecretId(role, payload)
        },
        read = { role, response ->
            appRole.readSecretId(role, response.secretId)!!
        }
    )
}

private suspend fun assertCreateCustomSecretIdWithBuilder(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedWritePath: String,
    expectedReadPath: String
) {
    assertCreateAndReadSecret<AuthAppRoleCreateCustomSecretIdPayload>(
        appRole,
        givenPath,
        expectedWritePath,
        expectedReadPath,
        defaultPayload = { error("Should not be called") },
        write = { role, payload ->
            appRole.createCustomSecretId(role) {
                this.secretId = payload.secretId
                this.metadata = payload.metadata
                this.cidrList = payload.cidrList
                this.tokenBoundCidrs = payload.tokenBoundCidrs
                this.numUses = payload.numUses
                this.ttl = payload.ttl
            }
        },
        read = { role, response ->
            appRole.readSecretId(role, response.secretId)!!
        }
    )
}

private suspend fun assertReadSecretIdAccessor(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedWritePath: String,
    expectedReadPath: String
) {
    assertCreateAndReadSecret<AuthAppRoleGenerateSecretIdPayload>(
        appRole,
        givenPath,
        expectedWritePath,
        expectedReadPath,
        defaultPayload = { AuthAppRoleGenerateSecretIdPayload() },
        write = { role, payload ->
            appRole.generateSecretId(role, payload)
        },
        read = { role, writeResponse ->
            appRole.readSecretIdAccessor(role, writeResponse.secretIdAccessor)
        }
    )
}

private suspend inline fun <reified P> assertCreateAndReadSecret(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedWritePath: String,
    expectedReadPath: String,
    defaultPayload: () -> P,
    write: (String, P) -> AuthAppRoleGenerateSecretIdResponse,
    read: (String, AuthAppRoleGenerateSecretIdResponse) -> AuthAppRoleReadSecretIdResponse
) {
    appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

    // Read the payload from the given path or use the default one
    val given = givenPath?.let { readJson<P>(it) } ?: defaultPayload()

    val writeResponse = write(DEFAULT_ROLE_NAME, given)
    val expectedWriteResponse = readJson<AuthAppRoleGenerateSecretIdResponse>(expectedWritePath)
    writeResponse shouldBe replaceTemplateString(expectedWriteResponse, writeResponse)

    val readResponse = read(DEFAULT_ROLE_NAME, writeResponse)
    val expectedReadResponse = readJson<AuthAppRoleReadSecretIdResponse>(expectedReadPath)
    readResponse shouldBe replaceTemplateString(expectedReadResponse, readResponse)
}

private suspend fun assertCreateRole(appRole: VaultAuthAppRole, givenPath: String?, expectedReadPath: String) {
    assertCreateRole(
        appRole,
        givenPath,
        expectedReadPath
    ) { role, payload ->
        appRole.createOrUpdate(role, payload)
    }
}

private suspend fun assertCreateRoleWithBuilder(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateRole(
        appRole,
        givenPath,
        expectedReadPath
    ) { role, payload ->
        appRole.createOrUpdate(role) {
            this.bindSecretId = payload.bindSecretId
            this.secretIdBoundCidrs = payload.secretIdBoundCidrs
            this.secretIdNumUses = payload.secretIdNumUses
            this.secretIdTTL = payload.secretIdTTL
            this.localSecretIds = payload.localSecretIds
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
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedReadPath: String,
    createOrUpdate: (String, AuthAppRoleCreateOrUpdatePayload) -> Boolean
) {
    val given = givenPath?.let { readJson<AuthAppRoleCreateOrUpdatePayload>(it) } ?: AuthAppRoleCreateOrUpdatePayload()
    createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true
    appRole.read(DEFAULT_ROLE_NAME) shouldBe readJson<AuthAppRoleReadResponse>(expectedReadPath)
}

private suspend fun assertLogin(appRole: VaultAuthAppRole, expectedWritePath: String) {
    assertLogin(
        appRole,
        expectedWritePath
    ) { roleId, secretId ->
        appRole.login(AuthAppRoleLoginPayload(roleId, secretId))
    }
}

private suspend fun assertLoginWithBuilder(appRole: VaultAuthAppRole, expectedWritePath: String) {
    assertLogin(
        appRole,
        expectedWritePath
    ) { roleId, secretId ->
        appRole.login {
            this.roleId = roleId
            this.secretId = secretId
        }
    }
}

private suspend inline fun assertLogin(
    appRole: VaultAuthAppRole,
    expectedWritePath: String,
    login: (String, String) -> LoginResponse
) {
    appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
    val secretId = appRole.generateSecretId(DEFAULT_ROLE_NAME).secretId
    val roleId = appRole.readRoleId(DEFAULT_ROLE_NAME).roleId

    val loginResponse = login(roleId, secretId)
    val expectedWriteResponse = readJson<LoginResponse>(expectedWritePath)

    loginResponse shouldBe replaceTemplateString(expectedWriteResponse, loginResponse)
}
