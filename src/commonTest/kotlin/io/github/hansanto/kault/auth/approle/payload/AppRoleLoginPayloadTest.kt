package io.github.hansanto.kault.auth.approle.payload

import io.github.hansanto.kault.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AppRoleLoginPayloadTest {

    @Test
    fun `should throw exception when mandatory fields are not set using builder`() = runTest {
        val builder = AppRoleLoginPayload.Builder()
        shouldThrow<Exception> {
            builder.build()
        }
    }

    @Test
    fun `should create instance with only mandatory fields using builder`() = runTest {
        val payload = AppRoleLoginPayload(
            roleId = randomString(),
            secretId = randomString()
        )

        AppRoleLoginPayload.Builder()
            .apply {
                roleId = payload.roleId
                secretId = payload.secretId
            }
            .build() shouldBe payload
    }
}
