package io.github.hansanto.kault.auth.kubernetes.payload

import io.github.hansanto.kault.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class KubernetesPayloadTest {

    @Test
    fun `should throw exception when mandatory fields are not set using builder`() = runTest {
        val builder = KubernetesLoginPayload.Builder()
        shouldThrow<Exception> {
            builder.build()
        }
    }

    @Test
    fun `should create instance with only mandatory fields using builder`() = runTest {
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
}
