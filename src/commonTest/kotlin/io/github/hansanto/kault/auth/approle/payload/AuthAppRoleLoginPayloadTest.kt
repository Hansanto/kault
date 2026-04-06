package io.github.hansanto.kault.auth.approle.payload

import io.github.hansanto.kault.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class AuthAppRoleLoginPayloadTest :
    ShouldSpec({

        should("throw exception when mandatory fields are not set using builder") {
            val builder = AuthAppRoleLoginPayload.Builder()
            shouldThrow<Exception> {
                builder.build()
            }
        }

        should("create instance with only mandatory fields using builder") {
            val payload = AuthAppRoleLoginPayload(
                roleId = randomString(),
                secretId = randomString()
            )

            AuthAppRoleLoginPayload.Builder()
                .apply {
                    roleId = payload.roleId
                    secretId = payload.secretId
                }
                .build() shouldBe payload
        }
    })
