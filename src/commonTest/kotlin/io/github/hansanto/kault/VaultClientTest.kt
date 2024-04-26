package io.github.hansanto.kault

import io.github.hansanto.kault.auth.common.common.TokenInfo
import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.auth.token.payload.TokenCreatePayload
import io.github.hansanto.kault.auth.token.response.TokenCreateResponse
import io.github.hansanto.kault.util.VAULT_URL
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.matcher.shouldBeBetween
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.revokeAllTokenData
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.comparables.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.ktor.utils.io.core.use
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

class VaultClientTest : ShouldSpec({

    beforeTest {
        createVaultClient().use {
            revokeAllTokenData(it)
        }
    }

    should("use default values if not set in builder") {
        val built = VaultClient {
            url = VAULT_URL
        }
        built.auth.autoRenewToken shouldBe true
    }

    should("use custom values in the builder") {
        val url = randomString()
        val namespace = randomString()
        val autoRenewToken = randomBoolean()

        val built = VaultClient {
            this.url = url
            this.namespace = namespace
            auth {
                this.autoRenewToken = autoRenewToken
            }
        }

        built.auth.autoRenewToken shouldBe autoRenewToken
    }

    should("cancel coroutine scope when closing client") {
        createVaultClient().use {
            it.client.coroutineContext.isActive shouldBe true
            it.close()
            it.client.coroutineContext.isActive shouldBe false
        }
    }

    should("not throw exception if lookup token is false without token") {
        shouldNotThrow<Exception> {
            VaultClient {
                url = VAULT_URL
                auth {
                    lookupToken = false
                }
            }
        }
    }

    should("throw exception if lookup token without token") {
        shouldThrow<IllegalArgumentException> {
            VaultClient {
                url = VAULT_URL
                auth {
                    lookupToken = true
                }
            }
        }.message shouldBe "When lookupToken is true, the token must be set"
    }

    should("lookup token with defined token") {
        val now = Clock.System.now()
        val createPayload = createSimpleTokenCreatePayload()
        val token = createVaultClient().use {
            it.auth.token.createToken(createPayload)
        }

        VaultClient {
            url = VAULT_URL
            auth {
                lookupToken = true
                setTokenString(token.clientToken)
            }
        }.use {
            val tokenInfo = it.auth.getTokenInfo()!!
            compareTokenInfo(
                tokenInfo,
                token,
                createPayload,
                now,
                createPayload.numUses!! - 1 // Lookup token will decrease the numUses
            )
        }
    }

    should("start renew token with the lookup token") {
        val now = Clock.System.now()
        val createPayload = createSimpleTokenCreatePayload()
        val token = createVaultClient().use {
            it.auth.token.createToken(createPayload)
        }

        VaultClient {
            url = VAULT_URL
            auth {
                lookupToken = true
                autoRenewToken = true
                setTokenString(token.clientToken)
                renewBeforeExpiration = 10.days
            }
        }.use {
            delay(100.milliseconds)
            val tokenInfo1 = it.auth.getTokenInfo()!!
            compareTokenInfo(
                tokenInfo1,
                token,
                createPayload,
                now,
                expectedNumUses = createPayload.numUses!! // Renew token will reset the numUses
            )

            delay(100.milliseconds)
            val tokenInfo2 = it.auth.getTokenInfo()!!
            tokenInfo2.expirationDate!! shouldBeGreaterThan tokenInfo1.expirationDate!!
        }
    }
})

private fun compareTokenInfo(
    tokenInfo: TokenInfo,
    token: TokenCreateResponse,
    createPayload: TokenCreatePayload,
    now: Instant,
    expectedNumUses: Long
) {
    val startRangeExpiration = now + token.leaseDuration

    tokenInfo.token shouldBe token.clientToken
    tokenInfo.accessor shouldBe token.accessor
    tokenInfo.expirationDate!! shouldBeBetween startRangeExpiration..(startRangeExpiration + 1.minutes)
    tokenInfo.tokenPolicies shouldContainExactlyInAnyOrder createPayload.policies!! + "default"
    tokenInfo.metadata shouldBe createPayload.metadata!!
    tokenInfo.renewable shouldBe createPayload.renewable!!
    tokenInfo.tokenType shouldBe createPayload.type!!
    tokenInfo.numUses shouldBe expectedNumUses
    tokenInfo.orphan shouldBe createPayload.noParent!!
    tokenInfo.entityId shouldBe token.entityId
}

private fun createSimpleTokenCreatePayload() = TokenCreatePayload(
    id = "my-token-id",
    policies = List(3) { "policy-$it" },
    metadata = mapOf("key" to "value"),
    noParent = true,
    noDefaultPolicy = false,
    renewable = true,
    ttl = 1.days,
    type = TokenType.SERVICE,
    explicitMaxTTL = 3.days,
    displayName = "my-token-display-name",
    numUses = 100
)
