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
import io.github.hansanto.kault.system.auth.enable
import io.github.hansanto.kault.util.DEFAULT_ROLE_NAME
import io.github.hansanto.kault.util.STRING_REPLACE
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class VaultAuthAppRoleTest : FunSpec({

    lateinit var client: VaultClient
    lateinit var appRole: VaultAuthAppRole

    beforeSpec {
        client = createVaultClient()
        appRole = client.auth.appRole

        runCatching {
            client.system.auth.enable("approle") {
                type = "approle"
            }
        }
    }

    afterSpec {
        client.close()
    }

    beforeTest {
        runCatching {
            appRole.list().forEach {
                appRole.delete(it) shouldBe true
            }
        }

        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    test("builder default variables should be set correctly") {
        VaultAuthAppRoleImpl.Default.PATH shouldBe "approle"

        val built = VaultAuthAppRoleImpl(client.client, null) {
        }

        built.path shouldBe VaultAuthAppRoleImpl.Default.PATH
    }

    test("builder should set values correctly") {
        val builderPath = randomString()
        val parentPath = randomString()

        val built = VaultAuthAppRoleImpl(client.client, parentPath) {
            path = builderPath
        }

        built.path shouldBe "$parentPath/$builderPath"
    }

    test("list with no roles") {
        shouldThrow<VaultAPIException> {
            appRole.list()
        }
    }

    test("list with roles") {
        val roles = List(10) { "test-$it" }
        roles.forEach { appRole.createOrUpdate(it) shouldBe true }
        appRole.list() shouldBe roles
    }

    test("create without options") {
        assertCreate(
            appRole,
            null,
            "cases/auth/approle/create/without_options/expected.json"
        )
    }

    test("create with options") {
        assertCreate(
            appRole,
            "cases/auth/approle/create/with_options/given.json",
            "cases/auth/approle/create/with_options/expected.json"
        )
    }

    test("update approle") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

        val given = readJson<AppRoleCreateOrUpdatePayload>("cases/auth/approle/update/given.json")
        appRole.createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true

        val expected = readJson<AppRoleReadRoleResponse>("cases/auth/approle/update/expected.json")
        appRole.read(DEFAULT_ROLE_NAME) shouldBe expected
    }

    test("read non-existing role") {
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    test("read existing role") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val response = appRole.read(DEFAULT_ROLE_NAME)
        response shouldNotBe null
    }

    test("delete non-existing role") {
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        appRole.delete(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    test("delete existing role") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldNotThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
        appRole.delete(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    test("read non-existing roleId") {
        shouldThrow<VaultAPIException> { appRole.readRoleID(DEFAULT_ROLE_NAME) }
    }

    test("read existing roleId") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val response = appRole.readRoleID(DEFAULT_ROLE_NAME)
        response.roleId.length shouldNotBe 0
    }

    test("update roleId with non-existing role") {
        shouldThrow<VaultAPIException> { appRole.updateRoleID(DEFAULT_ROLE_NAME, "test") }
    }

    test("update roleId with existing role") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

        val newRoleId = "test"
        val previousRoleId = appRole.readRoleID(DEFAULT_ROLE_NAME)
        previousRoleId.roleId shouldNotBe newRoleId

        val expectedResponse = AppRoleReadRoleIdResponse(newRoleId)
        appRole.updateRoleID(DEFAULT_ROLE_NAME, newRoleId) shouldBe true
        appRole.readRoleID(DEFAULT_ROLE_NAME) shouldBe expectedResponse
    }

    test("generate secret id with non-existing role") {
        shouldThrow<VaultAPIException> { appRole.generateSecretID(DEFAULT_ROLE_NAME) }
    }

    test("generate secret id with existing role without options") {
        assertGenerateSecretID(
            appRole,
            null,
            "cases/auth/approle/generate-secret-id/without_options/expected_write.json",
            "cases/auth/approle/generate-secret-id/without_options/expected_read.json"
        )
    }

    test("generate secret id with existing role with options and read it") {
        assertGenerateSecretID(
            appRole,
            "cases/auth/approle/generate-secret-id/with_options/given.json",
            "cases/auth/approle/generate-secret-id/with_options/expected_write.json",
            "cases/auth/approle/generate-secret-id/with_options/expected_read.json"
        )
    }

    test("secret id accessors with non-existing role") {
        shouldThrow<VaultAPIException> { appRole.secretIdAccessors(DEFAULT_ROLE_NAME) }
    }

    test("secret id accessors with existing role without secret id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

        shouldThrow<VaultAPIException> { appRole.secretIdAccessors(DEFAULT_ROLE_NAME) }
    }

    test("secret id accessors with existing role") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

        repeat(10) {
            appRole.generateSecretID(DEFAULT_ROLE_NAME)
            appRole.secretIdAccessors(DEFAULT_ROLE_NAME).size shouldBe it + 1
        }
    }

    test("read secret id non-existing role") {
        shouldThrow<VaultAPIException> { appRole.readSecretID(DEFAULT_ROLE_NAME, "test") }
    }

    test("read secret id existing role and non-existing secret id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        appRole.readSecretID(DEFAULT_ROLE_NAME, "test") shouldBe null
    }

    test("read secret id existing role and existing secret id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val generateResponse = appRole.generateSecretID(DEFAULT_ROLE_NAME)
        appRole.readSecretID(DEFAULT_ROLE_NAME, generateResponse.secretId) shouldNotBe null
    }

    test("destroy secret id with non-existing role") {
        shouldThrow<VaultAPIException> { appRole.destroySecretID(DEFAULT_ROLE_NAME, "test") }
    }

    test("destroy secret id with existing role and non-existing secret id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        appRole.destroySecretID(DEFAULT_ROLE_NAME, "test") shouldBe true
    }

    test("destroy secret id with existing role and existing secret id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val secretId = appRole.generateSecretID(DEFAULT_ROLE_NAME).secretId

        appRole.readSecretID(DEFAULT_ROLE_NAME, secretId) shouldNotBe null
        appRole.destroySecretID(DEFAULT_ROLE_NAME, secretId) shouldBe true
        appRole.readSecretID(DEFAULT_ROLE_NAME, secretId) shouldBe null
    }

    test("read secret id accessor with non-existing role") {
        shouldThrow<VaultAPIException> { appRole.readSecretIDAccessor(DEFAULT_ROLE_NAME, "test") }
    }

    test("read secret id accessor with existing role and non-existing secret id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { appRole.readSecretIDAccessor(DEFAULT_ROLE_NAME, "test") }
    }

    test("read secret id accessor without options & with existing role and existing secret id") {
        assertReadSecretIdAccessor(
            appRole,
            null,
            "cases/auth/approle/read-secret-id-accessor/without_options/expected_write.json",
            "cases/auth/approle/read-secret-id-accessor/without_options/expected_read.json"
        )
    }

    test("read secret id accessor with options & existing role and existing secret id") {
        assertReadSecretIdAccessor(
            appRole,
            "cases/auth/approle/read-secret-id-accessor/with_options/given.json",
            "cases/auth/approle/read-secret-id-accessor/with_options/expected_write.json",
            "cases/auth/approle/read-secret-id-accessor/with_options/expected_read.json"
        )
    }

    test("destroy secret id accessor with non-existing role") {
        shouldThrow<VaultAPIException> { appRole.destroySecretID(DEFAULT_ROLE_NAME, "test") }
    }

    test("destroy secret id accessor with existing role and non-existing secret id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { appRole.destroySecretIDAccessor(DEFAULT_ROLE_NAME, "test") }
    }

    test("destroy secret id accessor with existing role and existing secret id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val secretIdAccessor = appRole.generateSecretID(DEFAULT_ROLE_NAME).secretIdAccessor

        shouldNotThrow<VaultAPIException> { appRole.readSecretIDAccessor(DEFAULT_ROLE_NAME, secretIdAccessor) }
        appRole.destroySecretIDAccessor(DEFAULT_ROLE_NAME, secretIdAccessor) shouldBe true
        shouldThrow<VaultAPIException> { appRole.readSecretIDAccessor(DEFAULT_ROLE_NAME, secretIdAccessor) }
    }

    test("create custom secret id with non-existing role") {
        shouldThrow<VaultAPIException> {
            appRole.createCustomSecretID(
                DEFAULT_ROLE_NAME,
                AppRoleCreateCustomSecretIDPayload("")
            )
        }
    }

    test("create custom secret id with existing role without secret-id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> {
            appRole.createCustomSecretID(
                DEFAULT_ROLE_NAME,
                AppRoleCreateCustomSecretIDPayload("")
            )
        }
    }

    test("create custom secret id with existing role without options") {
        assertCreateCustomSecretID(
            appRole,
            "cases/auth/approle/create-custom-secret-id/without_options/given.json",
            "cases/auth/approle/create-custom-secret-id/without_options/expected_write.json",
            "cases/auth/approle/create-custom-secret-id/without_options/expected_read.json"
        )
    }

    test("create custom secret id with existing role with options and read it") {
        assertCreateCustomSecretID(
            appRole,
            "cases/auth/approle/create-custom-secret-id/with_options/given.json",
            "cases/auth/approle/create-custom-secret-id/with_options/expected_write.json",
            "cases/auth/approle/create-custom-secret-id/with_options/expected_read.json"
        )
    }

    test("login with non-existing role") {
        shouldThrow<VaultAPIException> { appRole.login(AppRoleLoginPayload(DEFAULT_ROLE_NAME, "")) }
    }

    test("login with existing role without secret-id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val roleId = appRole.readRoleID(DEFAULT_ROLE_NAME).roleId
        shouldThrow<VaultAPIException> { appRole.login(AppRoleLoginPayload(roleId, "")) }
    }

    test("login with existing role with secret-id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val secretId = appRole.generateSecretID(DEFAULT_ROLE_NAME).secretId
        val roleId = appRole.readRoleID(DEFAULT_ROLE_NAME).roleId

        val response = appRole.login(AppRoleLoginPayload(roleId, secretId))
        val expected = readJson<LoginResponse>("cases/auth/approle/login/expected.json")
            .copy(
                accessor = response.accessor,
                clientToken = response.clientToken,
                entityId = response.entityId,
                leaseDuration = response.leaseDuration
            )

        response shouldBe expected
    }

    test("tidy tokens should start internal task") {
        val warnings = appRole.tidyTokens()
        warnings.size shouldNotBe 0
    }
})

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
    crossinline defaultPayload: () -> P,
    crossinline write: suspend (String, P) -> AppRoleWriteSecretIdResponse,
    crossinline read: suspend (String, AppRoleWriteSecretIdResponse) -> AppRoleLookUpSecretIdResponse
) {
    appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

    // Read the payload from the given path or use the default one
    val given = givenPath?.let { readJson<P>(it) } ?: defaultPayload()

    val writeResponse = write(DEFAULT_ROLE_NAME, given)

    // Read the expected response from the expected path and copy the secretId and secretIdAccessor from the
    // response because they are randomly generated
    val expectedWriteResponse =
        readJson<AppRoleWriteSecretIdResponse>(expectedWritePath)
            .copy(secretIdAccessor = writeResponse.secretIdAccessor)
            .run {
                // When we know the secretId is randomly generated, we replace it with the one from the response
                if (secretId == STRING_REPLACE) {
                    copy(secretId = writeResponse.secretId)
                } else {
                    this
                }
            }

    writeResponse shouldBe expectedWriteResponse

    val readResponse = read(DEFAULT_ROLE_NAME, writeResponse)

    // Read the expected response from the expected path and copy the secretIdAccessor & dates from the response
    // because it is randomly generated
    val expectedReadResponse =
        readJson<AppRoleLookUpSecretIdResponse>(expectedReadPath).copy(
            secretIdAccessor = writeResponse.secretIdAccessor,
            creationTime = readResponse.creationTime,
            expirationTime = readResponse.expirationTime,
            lastUpdatedTime = readResponse.lastUpdatedTime
        )
    readResponse shouldBe expectedReadResponse
}

private suspend fun assertCreate(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedReadPath: String
) {
    val given = givenPath?.let { readJson<AppRoleCreateOrUpdatePayload>(it) } ?: AppRoleCreateOrUpdatePayload()
    appRole.createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true

    val response = appRole.read(DEFAULT_ROLE_NAME)
    val expected = readJson<AppRoleReadRoleResponse>(expectedReadPath)
    response shouldBe expected
}
