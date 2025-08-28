package io.github.hansanto.kault.auth.approle

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.approle.payload.AppRoleCreateCustomSecretIDPayload
import io.github.hansanto.kault.auth.approle.payload.AppRoleCreateOrUpdatePayload
import io.github.hansanto.kault.auth.approle.payload.AppRoleGenerateSecretIDPayload
import io.github.hansanto.kault.auth.approle.payload.AppRoleLoginPayload
import io.github.hansanto.kault.auth.approle.response.AppRoleLookUpSecretIdResponse
import io.github.hansanto.kault.auth.approle.response.AppRoleReadRoleIdResponse
import io.github.hansanto.kault.auth.approle.response.AppRoleReadRoleResponse
import io.github.hansanto.kault.auth.approle.response.AppRoleWriteSecretIdResponse
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
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class VaultAuthAppRoleTest {

    lateinit var client: VaultClient
    lateinit var appRole: VaultAuthAppRole

    @BeforeTest
    fun onBefore() = runTest {
        client = createVaultClient()
        appRole = client.auth.appRole

        enableAuthMethod(client, "approle")
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    @AfterTest
    fun onAfter() = runTest {
        revokeAllAppRoleData(client)
        client.close()
    }

    @Test
    fun `should use default path if not set in builder`() = runTest {
        VaultAuthAppRoleImpl.Default.PATH shouldBe "approle"

        val built = VaultAuthAppRoleImpl(client.client, null) {
        }

        built.path shouldBe VaultAuthAppRoleImpl.Default.PATH
    }

    @Test
    fun `should use custom path if set in builder`() = runTest {
        val builderPath = randomString()
        val parentPath = randomString()

        val built = VaultAuthAppRoleImpl(client.client, parentPath) {
            path = builderPath
        }

        built.path shouldBe "$parentPath/$builderPath"
    }

    @Test
    fun `should throw exception if no role was created when listing roles`() = runTest {
        shouldThrow<VaultAPIException> {
            appRole.list()
        }
    }

    @Test
    fun `should return created roles when listing`() = runTest {
        val roles = List(10) { "test-$it" }
        roles.forEach { appRole.createOrUpdate(it) shouldBe true }
        appRole.list() shouldBe roles
    }

    @Test
    fun `should create a role with default values`() = runTest {
        assertCreateRole(
            appRole,
            null,
            "cases/auth/approle/create/without_options/expected.json"
        )
    }

    @Test
    fun `should create a role with all defined values`() = runTest {
        assertCreateRole(
            appRole,
            "cases/auth/approle/create/with_options/given.json",
            "cases/auth/approle/create/with_options/expected.json"
        )
    }

    @Test
    fun `should create a role using builder with default values`() = runTest {
        assertCreateRoleWithBuilder(
            appRole,
            null,
            "cases/auth/approle/create/without_options/expected.json"
        )
    }

    @Test
    fun `should create a role using builder with all defined values`() = runTest {
        assertCreateRoleWithBuilder(
            appRole,
            "cases/auth/approle/create/with_options/given.json",
            "cases/auth/approle/create/with_options/expected.json"
        )
    }

    @Test
    fun `should update a role if it exists`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

        val given = readJson<AppRoleCreateOrUpdatePayload>("cases/auth/approle/update/given.json")
        appRole.createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true

        val expected = readJson<AppRoleReadRoleResponse>("cases/auth/approle/update/expected.json")
        appRole.read(DEFAULT_ROLE_NAME) shouldBe expected
    }

    @Test
    fun `should throw exception when reading non-existing role`() = runTest {
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    @Test
    fun `should read existing role`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val response = appRole.read(DEFAULT_ROLE_NAME)
        response shouldNotBe null
    }

    @Test
    fun `should do nothing when deleting non-existing role`() = runTest {
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        appRole.delete(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    @Test
    fun `should delete existing role`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldNotThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        appRole.delete(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    @Test
    fun `should throw exception when reading non-existing roleId`() = runTest {
        shouldThrow<VaultAPIException> { appRole.readRoleID(DEFAULT_ROLE_NAME) }
    }

    @Test
    fun `should read roleId with existing role`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val response = appRole.readRoleID(DEFAULT_ROLE_NAME)
        response.roleId.length shouldNotBe 0
    }

    @Test
    fun `should throw exception when updating roleId with non-existing role`() = runTest {
        shouldThrow<VaultAPIException> { appRole.updateRoleID(DEFAULT_ROLE_NAME, "test") }
    }

    @Test
    fun `should update roleId with existing role`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

        val newRoleId = "test"
        val previousRoleId = appRole.readRoleID(DEFAULT_ROLE_NAME)
        previousRoleId.roleId shouldNotBe newRoleId

        val expectedResponse = AppRoleReadRoleIdResponse(newRoleId)
        appRole.updateRoleID(DEFAULT_ROLE_NAME, newRoleId) shouldBe true
        appRole.readRoleID(DEFAULT_ROLE_NAME) shouldBe expectedResponse
    }

    @Test
    fun `should throw exception when generating secret id with non-existing role`() = runTest {
        shouldThrow<VaultAPIException> { appRole.generateSecretID(DEFAULT_ROLE_NAME) }
    }

    @Test
    fun `should generate secret id with existing role with default values`() = runTest {
        assertGenerateSecretID(
            appRole,
            null,
            "cases/auth/approle/generate_secret_id/without_options/expected_write.json",
            "cases/auth/approle/generate_secret_id/without_options/expected_read.json"
        )
    }

    @Test
    fun `should generate secret id using builder with existing role with default values`() = runTest {
        assertGenerateSecretIDWithBuilder(
            appRole,
            null,
            "cases/auth/approle/generate_secret_id/without_options/expected_write.json",
            "cases/auth/approle/generate_secret_id/without_options/expected_read.json"
        )
    }

    @Test
    fun `should generate secret id with existing role with all defined values`() = runTest {
        assertGenerateSecretID(
            appRole,
            "cases/auth/approle/generate_secret_id/with_options/given.json",
            "cases/auth/approle/generate_secret_id/with_options/expected_write.json",
            "cases/auth/approle/generate_secret_id/with_options/expected_read.json"
        )
    }

    @Test
    fun `should generate secret id using builder with existing role with all defined values`() = runTest {
        assertGenerateSecretIDWithBuilder(
            appRole,
            "cases/auth/approle/generate_secret_id/with_options/given.json",
            "cases/auth/approle/generate_secret_id/with_options/expected_write.json",
            "cases/auth/approle/generate_secret_id/with_options/expected_read.json"
        )
    }

    @Test
    fun `should throw exception when get secret id accessors with non-existing role`() = runTest {
        shouldThrow<VaultAPIException> { appRole.secretIdAccessors(DEFAULT_ROLE_NAME) }
    }

    @Test
    fun `should throw exception when get secret id accessors with existing role without secret id`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

        shouldThrow<VaultAPIException> { appRole.secretIdAccessors(DEFAULT_ROLE_NAME) }
    }

    @Test
    fun `should generate secret id accessors with existing role`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

        repeat(10) {
            appRole.generateSecretID(DEFAULT_ROLE_NAME)
            appRole.secretIdAccessors(DEFAULT_ROLE_NAME).size shouldBe it + 1
        }
    }

    @Test
    fun `should throw exception when read secret id with non-existing role`() = runTest {
        shouldThrow<VaultAPIException> { appRole.readSecretID(DEFAULT_ROLE_NAME, "test") }
    }

    @Test
    fun `should return null when read secret id with existing role and non-existing secret id`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        appRole.readSecretID(DEFAULT_ROLE_NAME, "test") shouldBe null
    }

    @Test
    fun `should read secret id with existing role and existing secret id`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val generateResponse = appRole.generateSecretID(DEFAULT_ROLE_NAME)
        appRole.readSecretID(DEFAULT_ROLE_NAME, generateResponse.secretId) shouldNotBe null
    }

    @Test
    fun `should throw exception when destroy secret id with non-existing role`() = runTest {
        shouldThrow<VaultAPIException> { appRole.destroySecretID(DEFAULT_ROLE_NAME, "test") }
    }

    @Test
    fun `should destroy secret id with existing role and non-existing secret id`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        appRole.destroySecretID(DEFAULT_ROLE_NAME, "test") shouldBe true
    }

    @Test
    fun `should destroy secret id with existing role and existing secret id`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val secretId = appRole.generateSecretID(DEFAULT_ROLE_NAME).secretId

        appRole.readSecretID(DEFAULT_ROLE_NAME, secretId) shouldNotBe null
        appRole.destroySecretID(DEFAULT_ROLE_NAME, secretId) shouldBe true
        appRole.readSecretID(DEFAULT_ROLE_NAME, secretId) shouldBe null
    }

    @Test
    fun `should throw exception when read secret id accessor with non-existing role`() = runTest {
        shouldThrow<VaultAPIException> { appRole.readSecretIDAccessor(DEFAULT_ROLE_NAME, "test") }
    }

    @Test
    fun `should read secret id accessor with existing role and non-existing secret id`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { appRole.readSecretIDAccessor(DEFAULT_ROLE_NAME, "test") }
    }

    @Test
    fun `should read secret id accessor with existing role and existing secret id with default values`() = runTest {
        assertReadSecretIdAccessor(
            appRole,
            null,
            "cases/auth/approle/read_secret_id_accessor/without_options/expected_write.json",
            "cases/auth/approle/read_secret_id_accessor/without_options/expected_read.json"
        )
    }

    @Test
    fun `should read secret id accessor with existing role and existing secret id with all defined values`() = runTest {
        assertReadSecretIdAccessor(
            appRole,
            "cases/auth/approle/read_secret_id_accessor/with_options/given.json",
            "cases/auth/approle/read_secret_id_accessor/with_options/expected_write.json",
            "cases/auth/approle/read_secret_id_accessor/with_options/expected_read.json"
        )
    }

    @Test
    fun `should throw exception when destroy secret id accessor with non-existing role`() = runTest {
        shouldThrow<VaultAPIException> { appRole.destroySecretIDAccessor(DEFAULT_ROLE_NAME, "test") }
    }

    @Test
    fun `should throw exception when destroy secret id accessor with existing role and non-existing secret id`() =
        runTest {
            appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
            shouldThrow<VaultAPIException> { appRole.destroySecretIDAccessor(DEFAULT_ROLE_NAME, "test") }
        }

    @Test
    fun `should destroy secret id accessor with existing role and existing secret id`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val secretIdAccessor = appRole.generateSecretID(DEFAULT_ROLE_NAME).secretIdAccessor

        shouldNotThrow<VaultAPIException> { appRole.readSecretIDAccessor(DEFAULT_ROLE_NAME, secretIdAccessor) }
        appRole.destroySecretIDAccessor(DEFAULT_ROLE_NAME, secretIdAccessor) shouldBe true
        shouldThrow<VaultAPIException> { appRole.readSecretIDAccessor(DEFAULT_ROLE_NAME, secretIdAccessor) }
    }

    @Test
    fun `should throw exception when create custom secret id with non-existing role`() = runTest {
        shouldThrow<VaultAPIException> {
            appRole.createCustomSecretID(
                DEFAULT_ROLE_NAME,
                AppRoleCreateCustomSecretIDPayload(randomString())
            )
        }
    }

    @Test
    fun `should create custom secret id with existing role with empty secret-id`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> {
            appRole.createCustomSecretID(
                DEFAULT_ROLE_NAME,
                AppRoleCreateCustomSecretIDPayload("")
            )
        }
    }

    @Test
    fun `should create custom secret id with existing role with default values with default values`() = runTest {
        assertCreateCustomSecretID(
            appRole,
            "cases/auth/approle/create_custom_secret_id/without_options/given.json",
            "cases/auth/approle/create_custom_secret_id/without_options/expected_write.json",
            "cases/auth/approle/create_custom_secret_id/without_options/expected_read.json"
        )
    }

    @Test
    fun `should create custom secret id using builder with existing role with default values with default values`() =
        runTest {
            assertCreateCustomSecretIDWithBuilder(
                appRole,
                "cases/auth/approle/create_custom_secret_id/without_options/given.json",
                "cases/auth/approle/create_custom_secret_id/without_options/expected_write.json",
                "cases/auth/approle/create_custom_secret_id/without_options/expected_read.json"
            )
        }

    @Test
    fun `should create custom secret id with existing role with all defined values`() = runTest {
        assertCreateCustomSecretID(
            appRole,
            "cases/auth/approle/create_custom_secret_id/with_options/given.json",
            "cases/auth/approle/create_custom_secret_id/with_options/expected_write.json",
            "cases/auth/approle/create_custom_secret_id/with_options/expected_read.json"
        )
    }

    @Test
    fun `should create custom secret id using builder with existing role with all defined values`() = runTest {
        assertCreateCustomSecretIDWithBuilder(
            appRole,
            "cases/auth/approle/create_custom_secret_id/with_options/given.json",
            "cases/auth/approle/create_custom_secret_id/with_options/expected_write.json",
            "cases/auth/approle/create_custom_secret_id/with_options/expected_read.json"
        )
    }

    @Test
    fun `should throw exception when login with non-existing role`() = runTest {
        shouldThrow<VaultAPIException> { appRole.login(AppRoleLoginPayload(DEFAULT_ROLE_NAME, randomString())) }
    }

    @Test
    fun `should throw exception when login with existing role with empty secret-id`() = runTest {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val roleId = appRole.readRoleID(DEFAULT_ROLE_NAME).roleId
        shouldThrow<VaultAPIException> { appRole.login(AppRoleLoginPayload(roleId, "")) }
    }

    @Test
    fun `should login with existing role with secret-id`() = runTest {
        assertLogin(
            appRole,
            "cases/auth/approle/login/expected.json"
        ) { roleId, secretId ->
            appRole.login(AppRoleLoginPayload(roleId, secretId))
        }
    }

    @Test
    fun `should login using builder with existing role with secret-id`() = runTest {
        assertLogin(
            appRole,
            "cases/auth/approle/login/expected.json"
        ) { roleId, secretId ->
            appRole.login {
                this.roleId = roleId
                this.secretId = secretId
            }
        }
    }

    @Test
    fun `should tidy tokens should start internal task`() = runTest {
        val warnings = appRole.tidyTokens()
        warnings.size shouldNotBe 0
    }
}

private suspend fun assertGenerateSecretID(
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
        defaultPayload = { AppRoleGenerateSecretIDPayload() },
        write = { role, payload ->
            appRole.generateSecretID(role, payload)
        },
        read = { role, response ->
            appRole.readSecretID(role, response.secretId)!!
        }
    )
}

private suspend fun assertGenerateSecretIDWithBuilder(
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
        defaultPayload = { AppRoleGenerateSecretIDPayload() },
        write = { role, payload ->
            appRole.generateSecretID(role) {
                this.metadata = payload.metadata
                this.cidrList = payload.cidrList
                this.tokenBoundCidrs = payload.tokenBoundCidrs
                this.numUses = payload.numUses
                this.ttl = payload.ttl
            }
        },
        read = { role, response ->
            appRole.readSecretID(role, response.secretId)!!
        }
    )
}

private suspend fun assertCreateCustomSecretID(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedWritePath: String,
    expectedReadPath: String
) {
    assertCreateAndReadSecret<AppRoleCreateCustomSecretIDPayload>(
        appRole,
        givenPath,
        expectedWritePath,
        expectedReadPath,
        defaultPayload = { error("Should not be called") },
        write = { role, payload ->
            appRole.createCustomSecretID(role, payload)
        },
        read = { role, response ->
            appRole.readSecretID(role, response.secretId)!!
        }
    )
}

private suspend fun assertCreateCustomSecretIDWithBuilder(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedWritePath: String,
    expectedReadPath: String
) {
    assertCreateAndReadSecret<AppRoleCreateCustomSecretIDPayload>(
        appRole,
        givenPath,
        expectedWritePath,
        expectedReadPath,
        defaultPayload = { error("Should not be called") },
        write = { role, payload ->
            appRole.createCustomSecretID(role) {
                this.secretId = payload.secretId
                this.metadata = payload.metadata
                this.cidrList = payload.cidrList
                this.tokenBoundCidrs = payload.tokenBoundCidrs
                this.numUses = payload.numUses
                this.ttl = payload.ttl
            }
        },
        read = { role, response ->
            appRole.readSecretID(role, response.secretId)!!
        }
    )
}

private suspend fun assertReadSecretIdAccessor(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedWritePath: String,
    expectedReadPath: String
) {
    assertCreateAndReadSecret<AppRoleGenerateSecretIDPayload>(
        appRole,
        givenPath,
        expectedWritePath,
        expectedReadPath,
        defaultPayload = { AppRoleGenerateSecretIDPayload() },
        write = { role, payload ->
            appRole.generateSecretID(role, payload)
        },
        read = { role, writeResponse ->
            appRole.readSecretIDAccessor(role, writeResponse.secretIdAccessor)
        }
    )
}

private suspend inline fun <reified P> assertCreateAndReadSecret(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedWritePath: String,
    expectedReadPath: String,
    defaultPayload: () -> P,
    write: (String, P) -> AppRoleWriteSecretIdResponse,
    read: (String, AppRoleWriteSecretIdResponse) -> AppRoleLookUpSecretIdResponse
) {
    appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

    // Read the payload from the given path or use the default one
    val given = givenPath?.let { readJson<P>(it) } ?: defaultPayload()

    val writeResponse = write(DEFAULT_ROLE_NAME, given)
    val expectedWriteResponse = readJson<AppRoleWriteSecretIdResponse>(expectedWritePath)
    writeResponse shouldBe replaceTemplateString(expectedWriteResponse, writeResponse)

    val readResponse = read(DEFAULT_ROLE_NAME, writeResponse)
    val expectedReadResponse = readJson<AppRoleLookUpSecretIdResponse>(expectedReadPath)
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
    createOrUpdate: (String, AppRoleCreateOrUpdatePayload) -> Boolean
) {
    val given = givenPath?.let { readJson<AppRoleCreateOrUpdatePayload>(it) } ?: AppRoleCreateOrUpdatePayload()
    createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true
    appRole.read(DEFAULT_ROLE_NAME) shouldBe readJson<AppRoleReadRoleResponse>(expectedReadPath)
}

private suspend inline fun assertLogin(
    appRole: VaultAuthAppRole,
    expectedWritePath: String,
    login: (String, String) -> LoginResponse
) {
    appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
    val secretId = appRole.generateSecretID(DEFAULT_ROLE_NAME).secretId
    val roleId = appRole.readRoleID(DEFAULT_ROLE_NAME).roleId

    val loginResponse = login(roleId, secretId)
    val expectedWriteResponse = readJson<LoginResponse>(expectedWritePath)

    loginResponse shouldBe replaceTemplateString(expectedWriteResponse, loginResponse)
}
