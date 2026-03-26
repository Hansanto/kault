package io.github.hansanto.kault.system.mounts

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.util.createVaultEnterpriseClient
import io.github.hansanto.kault.util.deleteAllNamespaces
import io.github.hansanto.kault.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class VaultSystemMountsTest :
    ShouldSpec({

        lateinit var client: VaultClient
        lateinit var mounts: VaultSystemMounts

        beforeTest {
            client = createVaultEnterpriseClient()
            mounts = client.system.mounts
        }

        afterTest {
            deleteAllNamespaces(client)
            client.close()
        }

        should("use default path if not set in builder") {
            VaultSystemMountsImpl.Default.PATH shouldBe "mounts"

            val built = VaultSystemMountsImpl.Companion(client.client, null) {
            }

            built.path shouldBe VaultSystemMountsImpl.Default.PATH
        }

        should("use custom values in the builder") {
            val randomPath = randomString()
            val parentPath = randomString()

            val built = VaultSystemMountsImpl.Companion(client.client, parentPath) {
                path = randomPath
            }

            built.path shouldBe "$parentPath/$randomPath"
        }

})
