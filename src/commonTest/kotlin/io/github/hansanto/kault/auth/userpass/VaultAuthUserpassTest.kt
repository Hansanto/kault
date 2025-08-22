package io.github.hansanto.kault.auth.userpass

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.userpass.payload.UserpassWriteUserPayload
import io.github.hansanto.kault.auth.userpass.response.UserpassReadUserResponse
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.util.DEFAULT_ROLE_NAME
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.enableAuthMethod
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.revokeAllUserpassData
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class VaultAuthUserpassTest :
    ShouldSpec({

        lateinit var client: VaultClient
        lateinit var userpass: VaultAuthUserpass

        beforeTest {
            client = createVaultClient()
            userpass = client.auth.userpass

            enableAuthMethod(client, "userpass")
            revokeAllUserpassData(client)
        }

        afterTest {
            client.close()
        }

        should("use default path if not set in builder") {
            VaultAuthUserpassImpl.Default.PATH shouldBe "userpass"

            val built = VaultAuthUserpassImpl(client.client, null) {
            }

            built.path shouldBe VaultAuthUserpassImpl.Default.PATH
        }

        should("use custom path if set in builder") {
            val builderPath = randomString()
            val parentPath = randomString()

            val built = VaultAuthUserpassImpl(client.client, parentPath) {
                path = builderPath
            }

            built.path shouldBe "$parentPath/$builderPath"
        }

        should("create a user with default values") {
            assertCreateOrUpdate(
                userpass,
                "cases/auth/userpass/create/without_options/given.json",
                "cases/auth/userpass/create/without_options/expected.json"
            )
        }

        should("create a user with all defined values") {
            assertCreateOrUpdate(
                userpass,
                "cases/auth/userpass/create/with_options/given.json",
                "cases/auth/userpass/create/with_options/expected.json"
            )
        }

        should("create a user using builder with default values") {
            assertCreateOrUpdateWithBuilder(
                userpass,
                "cases/auth/userpass/create/without_options/given.json",
                "cases/auth/userpass/create/without_options/expected.json"
            )
        }

        should("create a user using builder with all defined values") {
            assertCreateOrUpdateWithBuilder(
                userpass,
                "cases/auth/userpass/create/with_options/given.json",
                "cases/auth/userpass/create/with_options/expected.json"
            )
        }

        should("do nothing when deleting non-existing role") {
            shouldThrow<VaultAPIException> { userpass.read(DEFAULT_ROLE_NAME) }
            userpass.delete(DEFAULT_ROLE_NAME) shouldBe true
            shouldThrow<VaultAPIException> { userpass.read(DEFAULT_ROLE_NAME) }
        }

        should("delete existing user") {
            val username = DEFAULT_ROLE_NAME
            userpass.createOrUpdate(username) {
                password = randomString()
            }

            shouldNotThrow<VaultAPIException> {
                userpass.read(username)
            }

            userpass.delete(username) shouldBe true

            shouldThrow<VaultAPIException> {
                userpass.read(username)
            }
        }

        should("throw exception when updating password with non-existing user") {
            val username = DEFAULT_ROLE_NAME
            val password = randomString()
            shouldThrow<VaultAPIException> {
                userpass.updatePassword(username, password)
            }
        }

        should("update password with existing user") {
            val username = DEFAULT_ROLE_NAME
            val firstPassword = randomString()
            val secondPassword = randomString()
            userpass.createOrUpdate(username) {
                password = firstPassword
            }

            shouldNotThrow<VaultAPIException> {
                userpass.login(username, firstPassword)
            }

            shouldThrow<VaultAPIException> {
                userpass.login(username, secondPassword)
            }

            userpass.updatePassword(username, secondPassword) shouldBe true

            shouldThrow<VaultAPIException> {
                userpass.login(username, firstPassword)
            }

            shouldNotThrow<VaultAPIException> {
                userpass.login(username, secondPassword)
            }
        }

        should("throw exception when updating policies with non-existing user") {
            val username = DEFAULT_ROLE_NAME
            val policies = listOf(randomString())
            shouldThrow<VaultAPIException> {
                userpass.updatePolicies(username, policies)
            }
        }

        should("update policies with existing user") {
            val username = DEFAULT_ROLE_NAME
            val initialPolicies = List(3) { "old-policy-$it" }
            val newPolicies = listOf("new-policy")
            userpass.createOrUpdate(username) {
                password = randomString()
                tokenPolicies = initialPolicies
            }

            userpass.updatePolicies(username, newPolicies) shouldBe true

            val user = userpass.read(username)
            user.tokenPolicies shouldContainExactlyInAnyOrder newPolicies
        }

        should("throw exception if no role was created when listing users") {
            shouldThrow<VaultAPIException> {
                userpass.list()
            }
        }

        should("return created users when listing") {
            val users = List(3) { "test-$it" }
            users.forEach {
                userpass.createOrUpdate(it) {
                    password = randomString()
                }
            }
            userpass.list() shouldContainExactlyInAnyOrder users
        }
    })

private suspend fun assertCreateOrUpdate(userpass: VaultAuthUserpass, givenPath: String, expectedReadPath: String) {
    assertCreateOrUpdate(userpass, givenPath, expectedReadPath) { username, given ->
        userpass.createOrUpdate(username, given)
    }
}

private suspend fun assertCreateOrUpdateWithBuilder(
    userpass: VaultAuthUserpass,
    givenPath: String,
    expectedReadPath: String
) {
    assertCreateOrUpdate(userpass, givenPath, expectedReadPath) { username, given ->
        userpass.createOrUpdate(username) {
            password = given.password
            tokenTTL = given.tokenTTL
            tokenMaxTTL = given.tokenMaxTTL
            tokenPolicies = given.tokenPolicies
            tokenBoundCidrs = given.tokenBoundCidrs
            tokenExplicitMaxTTL = given.tokenExplicitMaxTTL
            tokenNoDefaultPolicy = given.tokenNoDefaultPolicy
            tokenNumUses = given.tokenNumUses
            tokenPeriod = given.tokenPeriod
            tokenType = given.tokenType
        }
    }
}

private suspend inline fun assertCreateOrUpdate(
    userpass: VaultAuthUserpass,
    givenPath: String,
    expectedReadPath: String,
    createOrUpdate: suspend (String, UserpassWriteUserPayload) -> Boolean
) {
    val username = randomString()
    val given = readJson<UserpassWriteUserPayload>(givenPath)
    createOrUpdate(username, given) shouldBe true
    userpass.read(username) shouldBe readJson<UserpassReadUserResponse>(expectedReadPath)
}
