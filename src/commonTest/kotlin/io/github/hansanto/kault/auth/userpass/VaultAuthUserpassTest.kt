package io.github.hansanto.kault.auth.userpass

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.userpass.payload.UserpassWriteUserPayload
import io.github.hansanto.kault.system.auth.enable
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.core.spec.style.FunSpec
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
})

private suspend fun assertCreate(
    userpass: VaultAuthUserpass,
    givenPath: String,
    expectedReadPath: String
) {
    val username = randomString()
    val given = readJson<UserpassWriteUserPayload>(givenPath)
    userpass.createOrUpdate(username, given) shouldBe true
    // TODO: Check read username
}
