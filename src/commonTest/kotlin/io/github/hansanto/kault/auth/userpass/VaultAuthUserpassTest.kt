package io.github.hansanto.kault.auth.userpass

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.userpass.payload.UserpassWriteUserPayload
import io.github.hansanto.kault.auth.userpass.response.ReadUserResponse
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.system.auth.enable
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class VaultAuthUserpassTest : FunSpec({

    lateinit var client: VaultClient
    lateinit var userpass: VaultAuthUserpass

    beforeSpec {
        client = createVaultClient()
        userpass = client.auth.userpass

        runCatching {
            client.system.auth.enable("userpass") {
                type = "userpass"
            }
        }
    }

    afterSpec {
        client.close()
    }

    beforeTest {
        runCatching {
            userpass.list()
        }.getOrNull()?.forEach {
            userpass.delete(it)
        }
    }

    test("create without options") {
        assertCreate(
            userpass,
            "cases/auth/userpass/create/without_options/given.json",
            "cases/auth/userpass/create/without_options/expected.json"
        )
    }

    test("create with options") {
        assertCreate(
            userpass,
            "cases/auth/userpass/create/with_options/given.json",
            "cases/auth/userpass/create/with_options/expected.json"
        )
    }

    test("delete with non existing user") {
        val username = randomString()
        userpass.delete(username) shouldBe true

        shouldThrow<VaultAPIException> {
            userpass.read(username)
        }
    }

    test("delete with existing user") {
        val username = randomString()
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

    test("update password with non existing user") {
        val username = randomString()
        val password = randomString()
        shouldThrow<VaultAPIException> {
            userpass.updatePassword(username, password)
        }
    }

    test("update password with existing user") {
        val username = randomString()
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

    test("update policies with non existing user") {
        val username = randomString()
        val policies = listOf(randomString())
        shouldThrow<VaultAPIException> {
            userpass.updatePolicies(username, policies)
        }
    }

    test("update policies with existing user") {
        val username = randomString()
        val initialPolicies = List(3) { randomString() }
        val newPolicies = listOf(randomString())
        userpass.createOrUpdate(username) {
            password = randomString()
            tokenPolicies = initialPolicies
        }

        userpass.updatePolicies(username, newPolicies) shouldBe true

        val user = userpass.read(username)
        user.tokenPolicies shouldBe newPolicies
    }

    test("list with no users") {
        shouldThrow<VaultAPIException> {
            userpass.list()
        }
    }

    test("list with users") {
        val users = List(3) { randomString() }
        users.forEach {
            userpass.createOrUpdate(it) {
                password = randomString()
            }
        }
        userpass.list() shouldContainExactlyInAnyOrder users
    }
})

private suspend fun assertCreate(
    userpass: VaultAuthUserpass,
    givenPath: String,
    expectedReadPath: String
) {
    val username = randomString()
    val given = readJson<UserpassWriteUserPayload>(givenPath)
    userpass.createOrUpdate(username, given) shouldBe true

    val response = userpass.read(username)
    val expected = readJson<ReadUserResponse>(expectedReadPath)
    response shouldBe expected
}
