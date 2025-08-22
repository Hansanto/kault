package io.github.hansanto.kault.auth.kubernetes.payload

import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class KubernetesConfigureAuthPayloadTest :
    ShouldSpec({

        should("throw exception when mandatory fields are not set using builder") {
            val builder = KubernetesConfigureAuthPayload.Builder()
                .apply {
                    kubernetesCaCert = randomString()
                    pemKeys = List(5) { randomString() }
                    disableLocalCaJwt = randomBoolean()
                    tokenReviewerJwt = randomString()
                }

            shouldThrow<Exception> {
                builder.build()
            }
        }

        should("create instance with only mandatory fields using builder") {
            val payload = KubernetesConfigureAuthPayload(
                kubernetesHost = randomString()
            )

            KubernetesConfigureAuthPayload.Builder()
                .apply {
                    kubernetesHost = payload.kubernetesHost
                }
                .build() shouldBe payload
        }

        should("create instance with all fields using builder") {
            val payload = KubernetesConfigureAuthPayload(
                kubernetesHost = randomString(),
                kubernetesCaCert = randomString(),
                pemKeys = List(5) { randomString() },
                disableLocalCaJwt = randomBoolean(),
                tokenReviewerJwt = randomString()
            )

            KubernetesConfigureAuthPayload.Builder()
                .apply {
                    kubernetesHost = payload.kubernetesHost
                    kubernetesCaCert = payload.kubernetesCaCert
                    pemKeys = payload.pemKeys
                    disableLocalCaJwt = payload.disableLocalCaJwt
                    tokenReviewerJwt = payload.tokenReviewerJwt
                }
                .build() shouldBe payload
        }
    })
