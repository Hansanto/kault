package com.github.hansanto.kault.auth.approle

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.auth.approle.payload.CreateOrUpdatePayload
import com.github.hansanto.kault.auth.approle.payload.GenerateSecretIDPayload
import com.github.hansanto.kault.auth.approle.response.AppRoleLookUpSecretIdResponse
import com.github.hansanto.kault.auth.approle.response.AppRoleReadRoleIdResponse
import com.github.hansanto.kault.auth.approle.response.AppRoleReadRoleResponse
import com.github.hansanto.kault.auth.approle.response.AppRoleWriteSecretIdResponse
import com.github.hansanto.kault.exception.VaultAPIException
import com.github.hansanto.kault.system.auth.enableMethod
import com.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.flow.toList

private const val DEFAULT_ROLE_NAME = "test"

class VaultAuthAppRoleTest : FunSpec({

    lateinit var appRole: VaultAuthAppRole

    beforeSpec {
        val client = VaultClient {
            url = "http://localhost:8200"
        }

        client.auth.token = "root"
        appRole = client.auth.appRole

        runCatching {
            client.system.auth.enableMethod("approle") {
                type = "approle"
            } shouldBe true
        }
    }

    beforeTest {
        runCatching {
            appRole.list().collect {
                appRole.delete(it) shouldBe true
            }
        }

        shouldThrow<VaultAPIException> { appRole.read(DEFAULT_ROLE_NAME) }
    }

    test("list with no roles") {
        shouldThrow<VaultAPIException> {
            appRole.list().toList()
        }
    }

    test("list with roles") {
        val roles = List(10) { "test-$it" }
        roles.forEach { appRole.createOrUpdate(it) shouldBe true }
        appRole.list().toList() shouldBe roles
    }

    test("create without options and read it") {
        assertCreate(
            appRole,
            null,
            "cases/auth/approle/create/without_options/expected.json"
        )
    }

    test("create with options and read it") {
        assertCreate(
            appRole,
            "cases/auth/approle/create/with_options/given.json",
            "cases/auth/approle/create/with_options/expected.json"
        )
    }

    test("update approle") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

        val given = readJson<CreateOrUpdatePayload>("cases/auth/approle/update/given.json")
        appRole.createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true

        val expected = readJson<AppRoleReadRoleResponse>("cases/auth/approle/update/expected.json")
        appRole.read(DEFAULT_ROLE_NAME) shouldBe expected
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
            appRole.secretIdAccessors(DEFAULT_ROLE_NAME).keys.size shouldBe it + 1
        }
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
        appRole.destroySecretID(DEFAULT_ROLE_NAME, secretId) shouldBe true
    }

    test("read secret id accessor with non-existing role") {
        shouldThrow<VaultAPIException> { appRole.readSecretIDAccessor(DEFAULT_ROLE_NAME, "test") }
    }

    test("read secret id accessor with existing role and non-existing secret id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { appRole.readSecretIDAccessor(DEFAULT_ROLE_NAME, "test") }
    }

    test("read secret id accessor with existing role and existing secret id") {
        appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true
        val secretId = appRole.generateSecretID(DEFAULT_ROLE_NAME).secretId
        appRole.readSecretIDAccessor(DEFAULT_ROLE_NAME, secretId) shouldNotBe null
    }

})

private suspend fun assertGenerateSecretID(
    appRole: VaultAuthAppRole,
    givenPath: String?,
    expectedWritePath: String,
    expectedReadPath: String
) {
    appRole.createOrUpdate(DEFAULT_ROLE_NAME) shouldBe true

    // Read the payload from the given path or use the default one
    val given = givenPath?.let { readJson<GenerateSecretIDPayload>(it) } ?: GenerateSecretIDPayload()

    val writeResponse = appRole.generateSecretID(DEFAULT_ROLE_NAME, given)

    // Read the expected response from the expected path and copy the secretId and secretIdAccessor from the
    // response because they are randomly generated
    val expectedWriteResponse =
        readJson<AppRoleWriteSecretIdResponse>(expectedWritePath)
            .copy(secretId = writeResponse.secretId, secretIdAccessor = writeResponse.secretIdAccessor)

    writeResponse shouldBe expectedWriteResponse

    val readResponse = appRole.readSecretID(DEFAULT_ROLE_NAME, writeResponse.secretId)

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
    val given = givenPath?.let { readJson<CreateOrUpdatePayload>(it) } ?: CreateOrUpdatePayload()
    appRole.createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true

    val response = appRole.read(DEFAULT_ROLE_NAME)
    val expected = readJson<AppRoleReadRoleResponse>(expectedReadPath)
    response shouldBe expected
}
