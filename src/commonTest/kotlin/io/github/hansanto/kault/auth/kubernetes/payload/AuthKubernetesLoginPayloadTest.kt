package io.github.hansanto.kault.auth.kubernetes.payload

import io.github.hansanto.kault.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class AuthKubernetesLoginPayloadTest :
    ShouldSpec({

        should("throw exception when mandatory fields are not set using builder") {
            val builder = AuthKubernetesLoginPayload.Builder()
            shouldThrow<Exception> {
                builder.build()
            }
        }

        should("create instance with only mandatory fields using builder") {
            val payload = AuthKubernetesLoginPayload(
                role = randomString(),
                jwt = randomString()
            )

            AuthKubernetesLoginPayload.Builder()
                .apply {
                    role = payload.role
                    jwt = payload.jwt
                }
                .build() shouldBe payload
        }
    })
