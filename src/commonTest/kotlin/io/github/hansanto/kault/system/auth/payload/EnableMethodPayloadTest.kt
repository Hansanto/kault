package io.github.hansanto.kault.system.auth.payload

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class EnableMethodPayloadTest {

    @Test
    fun `should build builder with only 'type' field`() = runTest {
        shouldThrow<Exception> { EnableMethodPayload.Builder().build() }

        val type = "test"
        val payload = EnableMethodPayload.Builder().apply {
            this.type = type
        }.build()
        payload.type shouldBe type
    }
}
