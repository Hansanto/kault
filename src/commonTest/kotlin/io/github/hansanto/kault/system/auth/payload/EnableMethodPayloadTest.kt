package io.github.hansanto.kault.system.auth.payload

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class EnableMethodPayloadTest : FunSpec({

    test("builder type must be set") {
        shouldThrow<Exception> { EnableMethodPayload.Builder().build() }

        val type = "test"
        val payload = EnableMethodPayload.Builder().apply {
            this.type = type
        }.build()
        payload.type shouldBe type
    }
})
