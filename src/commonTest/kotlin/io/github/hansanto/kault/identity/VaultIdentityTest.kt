package io.github.hansanto.kault.identity

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.identity.entity.VaultIdentityEntityImpl
import io.github.hansanto.kault.identity.oidc.VaultIdentityOIDCImpl
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class VaultIdentityTest :
    ShouldSpec({

        lateinit var client: VaultClient

        beforeTest {
            client = createVaultClient()
        }

        afterTest {
            client.close()
        }

        should("use default path if not set in builder") {
            VaultIdentity.Default.PATH shouldBe "identity"

            val built = VaultIdentity(client.client, null) {
            }

            (built.entity as VaultIdentityEntityImpl).path shouldBe "${VaultIdentity.Default.PATH}/${VaultIdentityEntityImpl.Default.PATH}"
            (built.oidc as VaultIdentityOIDCImpl).path shouldBe "${VaultIdentity.Default.PATH}/${VaultIdentityOIDCImpl.Default.PATH}"
        }

        should("use custom path if set in builder") {
            val builderPath = randomString()
            val parentPath = randomString()

            val entity = randomString()
            val oidc = randomString()

            val built = VaultIdentity(client.client, parentPath) {
                path = builderPath
                entity {
                    path = entity
                }
                oidc {
                    path = oidc
                }
            }

            (built.entity as VaultIdentityEntityImpl).path shouldBe "$parentPath/$builderPath/$entity"
            (built.oidc as VaultIdentityOIDCImpl).path shouldBe "$parentPath/$builderPath/$oidc"
        }
    })
