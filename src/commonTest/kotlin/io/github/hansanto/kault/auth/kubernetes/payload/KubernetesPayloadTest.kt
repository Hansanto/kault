package io.github.hansanto.kault.auth.kubernetes.payload

import io.github.hansanto.kault.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class KubernetesPayloadTest :
    ShouldSpec({

        should("throw exception when mandatory fields are not set using builder") {
            val builder = KubernetesLoginPayload.Builder()
            shouldThrow<Exception> {
                builder.build()
            }
        }

        should("create instance with only mandatory fields using builder") {
            val payload = KubernetesLoginPayload(
                role = randomString(),
                jwt = randomString()
            )

            KubernetesLoginPayload.Builder()
                .apply {
                    role = payload.role
                    jwt = payload.jwt
                }
                .build() shouldBe payload
        }
    })
