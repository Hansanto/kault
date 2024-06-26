package io.github.hansanto.kault.auth.userpass.payload

import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomLong
import io.github.hansanto.kault.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds

class UserpassWriteUserPayloadTest : ShouldSpec({

    should("throw exception when mandatory fields are not set using builder") {
        val builder = UserpassWriteUserPayload.Builder()
            .apply {
                tokenTTL = randomLong().milliseconds
                tokenMaxTTL = randomLong().milliseconds
                tokenPolicies = listOf(randomString(), randomString())
                tokenBoundCidrs = listOf(randomString(), randomString())
                tokenExplicitMaxTTL = randomLong().milliseconds
                tokenNoDefaultPolicy = randomBoolean()
                tokenNumUses = randomLong()
                tokenPeriod = randomLong().milliseconds
                tokenType = TokenType.BATCH
            }

        shouldThrow<Exception> {
            builder.build()
        }
    }

    should("create instance with only mandatory fields using builder") {
        val payload = UserpassWriteUserPayload(
            password = randomString()
        )

        UserpassWriteUserPayload.Builder()
            .apply {
                password = payload.password
            }
            .build() shouldBe payload
    }

    should("create instance with all fields using builder") {
        val payload = UserpassWriteUserPayload(
            password = randomString(),
            tokenTTL = randomLong().milliseconds,
            tokenMaxTTL = randomLong().milliseconds,
            tokenPolicies = listOf(randomString(), randomString()),
            tokenBoundCidrs = listOf(randomString(), randomString()),
            tokenExplicitMaxTTL = randomLong().milliseconds,
            tokenNoDefaultPolicy = randomBoolean(),
            tokenNumUses = randomLong(),
            tokenPeriod = randomLong().milliseconds,
            tokenType = TokenType.BATCH
        )

        UserpassWriteUserPayload.Builder()
            .apply {
                password = payload.password
                tokenTTL = payload.tokenTTL
                tokenMaxTTL = payload.tokenMaxTTL
                tokenPolicies = payload.tokenPolicies
                tokenBoundCidrs = payload.tokenBoundCidrs
                tokenExplicitMaxTTL = payload.tokenExplicitMaxTTL
                tokenNoDefaultPolicy = payload.tokenNoDefaultPolicy
                tokenNumUses = payload.tokenNumUses
                tokenPeriod = payload.tokenPeriod
                tokenType = payload.tokenType
            }
            .build() shouldBe payload
    }
})
