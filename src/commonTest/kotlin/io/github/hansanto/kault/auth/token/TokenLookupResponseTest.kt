package io.github.hansanto.kault.auth.token

import io.github.hansanto.kault.auth.common.common.TokenInfo
import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.auth.token.response.TokenLookupResponse
import io.github.hansanto.kault.auth.token.response.toTokenInfo
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomInstant
import io.github.hansanto.kault.util.randomLong
import io.github.hansanto.kault.util.randomString
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds

class TokenLookupResponseTest {

    @Test
    fun `should transform TokenLookupResponse to TokenInfo`() = runTest {
        val response = TokenLookupResponse(
            accessor = randomString(),
            creationTime = randomLong(),
            creationTTL = randomLong().milliseconds,
            displayName = randomString(),
            entityId = randomString(),
            expireTime = randomInstant(),
            explicitMaxTTL = randomLong().milliseconds,
            id = randomString(),
            issueTime = randomInstant(),
            metadata = mapOf(
                randomString() to randomString(),
                randomString() to randomString()
            ),
            numUses = randomLong(),
            orphan = randomBoolean(),
            path = randomString(),
            policies = listOf(randomString(), randomString()),
            renewable = randomBoolean(),
            ttl = randomLong().milliseconds,
            tokenType = TokenType.entries.random()
        )

        val token = randomString()
        val toTokenInfo = response.toTokenInfo(token)
        toTokenInfo shouldBe TokenInfo(
            token = token,
            accessor = response.accessor,
            tokenPolicies = response.policies,
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
