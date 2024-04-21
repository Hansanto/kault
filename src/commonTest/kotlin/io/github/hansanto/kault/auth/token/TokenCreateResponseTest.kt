package io.github.hansanto.kault.auth.token

import io.github.hansanto.kault.auth.common.common.TokenInfo
import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.auth.token.response.TokenCreateResponse
import io.github.hansanto.kault.auth.token.response.toTokenInfo
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomLong
import io.github.hansanto.kault.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds

class TokenCreateResponseTest : ShouldSpec({

    should("transform TokenCreateResponse to TokenInfo") {
        val response = TokenCreateResponse(
            clientToken = randomString(),
            accessor = randomString(),
            policies = List(5) { randomString() },
            tokenPolicies = List(5) { randomString() },
            metadata = mapOf(randomString() to randomString()),
            leaseDuration = randomLong().milliseconds,
            renewable = randomBoolean(),
            entityId = randomString(),
            tokenType = TokenType.entries.random(),
            orphan = randomBoolean(),
            numUses = randomLong()
        )

        val toTokenInfo = response.toTokenInfo()
        toTokenInfo shouldBe TokenInfo(
            token = response.clientToken,
            accessor = response.accessor,
            tokenPolicies = response.tokenPolicies,
            metadata = response.metadata!!,
            expirationDate = toTokenInfo.expirationDate,
            renewable = response.renewable,
            entityId = response.entityId,
            tokenType = response.tokenType,
            orphan = response.orphan,
            numUses = response.numUses
        )
    }
})
