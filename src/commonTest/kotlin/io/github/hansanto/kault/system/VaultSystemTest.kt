package io.github.hansanto.kault.system

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.system.audit.VaultSystemAuditImpl
import io.github.hansanto.kault.system.auth.VaultSystemAuthImpl
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class VaultSystemTest {

    lateinit var client: VaultClient

    @BeforeTest
    fun onBefore() = runTest {
        client = createVaultClient()
    }

    @AfterTest
    fun onAfter() = runTest {
        client.close()
    }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `should use default path if not set in builder`() = runTest {
        VaultSystem.Default.PATH shouldBe "sys"
        val built = VaultSystem(client.client, null) {
        }

        (built.auth as VaultSystemAuthImpl).path shouldBe
            "${VaultSystem.Default.PATH}/${VaultSystemAuthImpl.Default.PATH}"
        (built.audit as VaultSystemAuditImpl).path shouldBe
            "${VaultSystem.Default.PATH}/${VaultSystemAuditImpl.Default.PATH}"
    }

    @Test
    fun `should use custom path if set in builder`() = runTest {
        val parentPath = randomString()
        val builderPath = randomString()
        val authPath = randomString()
        val auditPath = randomString()

        val built = VaultSystem(client.client, parentPath) {
            path = builderPath
            auth {
                path = authPath
            }
            audit {
                path = auditPath
            }
        }

        (built.auth as VaultSystemAuthImpl).path shouldBe "$parentPath/$builderPath/$authPath"
        (built.audit as VaultSystemAuditImpl).path shouldBe "$parentPath/$builderPath/$auditPath"
    }
}
