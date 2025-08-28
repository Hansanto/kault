package io.github.hansanto.kault.auth.token

import io.github.hansanto.kault.auth.common.common.TokenInfo
import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.auth.token.response.TokenCreateResponse
import io.github.hansanto.kault.auth.token.response.toTokenInfo
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomLong
import io.github.hansanto.kault.util.randomString
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

class TokenCreateResponseTest {

    @Test
    fun `should transform to TokenInfo and set to null expiration if leaseDuration is zero`() = runTest {
        val response = TokenCreateResponse(
            clientToken = randomString(),
            accessor = randomString(),
            policies = List(5) { randomString() },
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
            metadata = response.metadata!!,
            expirationDate = null,
            renewable = response.renewable,
            entityId = response.entityId,
            tokenType = response.tokenType,
            orphan = response.orphan,
            numUses = response.numUses
        )
    }

    @Test
    fun `should transform to TokenInfo`() = runTest {
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
}
