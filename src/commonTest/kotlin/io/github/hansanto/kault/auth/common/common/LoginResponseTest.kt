package io.github.hansanto.kault.auth.common.common

import io.github.hansanto.kault.auth.common.response.LoginResponse
import io.github.hansanto.kault.auth.common.response.toTokenInfo
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomLong
import io.github.hansanto.kault.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds

class LoginResponseTest : ShouldSpec({

    should("transform to TokenInfo and set to null expiration if leaseDuration is zero") {
        val response = LoginResponse(
            clientToken = randomString(),
            accessor = randomString(),
            tokenPolicies = List(5) { randomString() },
            metadata = mapOf(randomString() to randomString()),
            leaseDuration = 0L.milliseconds,
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
            metadata = response.metadata,
            expirationDate = null,
            renewable = response.renewable,
            entityId = response.entityId,
            tokenType = response.tokenType,
            orphan = response.orphan,
            numUses = response.numUses
        )
    }

    should("transform to TokenInfo") {
        val response = LoginResponse(
            clientToken = randomString(),
            accessor = randomString(),
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
            metadata = response.metadata,
            expirationDate = toTokenInfo.expirationDate,
            renewable = response.renewable,
            entityId = response.entityId,
            tokenType = response.tokenType,
            orphan = response.orphan,
            numUses = response.numUses
        )
    }
})
