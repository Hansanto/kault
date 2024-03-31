package io.github.hansanto.kault.system.auth.payload

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class EnableMethodPayloadTest : ShouldSpec({

    should("build builder with only 'type' field") {
        shouldThrow<Exception> { EnableMethodPayload.Builder().build() }

        val type = "test"
        val payload = EnableMethodPayload.Builder().apply {
            this.type = type
        }.build()
        payload.type shouldBe type
    }
})
