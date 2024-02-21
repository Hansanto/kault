package io.github.hansanto.kault.system

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.system.audit.VaultSystemAuditImpl
import io.github.hansanto.kault.system.auth.VaultSystemAuthImpl
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VaultSystemTest : FunSpec({

    lateinit var client: VaultClient

    beforeSpec {
        client = createVaultClient()
    }

    afterSpec {
        client.close()
    }

    test("builder default variables should be set correctly") {
        VaultSystem.Default.PATH shouldBe "sys"
        val built = VaultSystem(client.client, null) {
        }

        (built.auth as VaultSystemAuthImpl).path shouldBe "${VaultSystem.Default.PATH}/${VaultSystemAuthImpl.Default.PATH}"
        (built.audit as VaultSystemAuditImpl).path shouldBe "${VaultSystem.Default.PATH}/${VaultSystemAuditImpl.Default.PATH}"
    }

    test("builder should set values correctly") {
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
})
