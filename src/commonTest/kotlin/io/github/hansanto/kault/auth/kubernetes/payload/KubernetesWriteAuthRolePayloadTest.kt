package io.github.hansanto.kault.auth.kubernetes.payload

import io.github.hansanto.kault.auth.common.common.TokenType
import io.github.hansanto.kault.auth.kubernetes.common.KubernetesAliasNameSourceType
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomLong
import io.github.hansanto.kault.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds

class KubernetesWriteAuthRolePayloadTest : ShouldSpec({

    should("throw exception when mandatory fields are not set using builder") {
        val builder = KubernetesWriteAuthRolePayload.Builder()
            .apply {
                audience = randomString()
                aliasNameSource = KubernetesAliasNameSourceType.entries.random()
                tokenTTL = randomLong().milliseconds
                tokenMaxTTL = randomLong().milliseconds
                tokenPolicies = List(5) { randomString() }
                tokenBoundCidrs = List(5) { randomString() }
                tokenExplicitMaxTTL = randomLong().milliseconds
                tokenNoDefaultPolicy = randomBoolean()
                tokenNumUses = randomLong()
                tokenPeriod = randomLong().milliseconds
                tokenType = TokenType.entries.random()
            }

        shouldThrow<Exception> {
            builder.build()
        }
    }

    should("create instance with only mandatory fields using builder") {
        val payload = KubernetesWriteAuthRolePayload(
            boundServiceAccountNames = List(5) { randomString() },
            boundServiceAccountNamespaces = List(5) { randomString() }
        )

        KubernetesWriteAuthRolePayload.Builder()
            .apply {
                boundServiceAccountNames = payload.boundServiceAccountNames
                boundServiceAccountNamespaces = payload.boundServiceAccountNamespaces
            }
            .build() shouldBe payload
    }

    should("create instance with all fields using builder") {
        val payload = KubernetesWriteAuthRolePayload(
            boundServiceAccountNames = List(5) { randomString() },
            boundServiceAccountNamespaces = List(5) { randomString() },
            audience = randomString(),
            aliasNameSource = KubernetesAliasNameSourceType.entries.random(),
            tokenTTL = randomLong().milliseconds,
            tokenMaxTTL = randomLong().milliseconds,
            tokenPolicies = List(5) { randomString() },
            tokenBoundCidrs = List(5) { randomString() },
            tokenExplicitMaxTTL = randomLong().milliseconds,
            tokenNoDefaultPolicy = randomBoolean(),
            tokenNumUses = randomLong(),
            tokenPeriod = randomLong().milliseconds,
            tokenType = TokenType.entries.random()
        )

        KubernetesWriteAuthRolePayload.Builder()
            .apply {
                boundServiceAccountNames = payload.boundServiceAccountNames
                boundServiceAccountNamespaces = payload.boundServiceAccountNamespaces
                audience = payload.audience
                aliasNameSource = payload.aliasNameSource
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
