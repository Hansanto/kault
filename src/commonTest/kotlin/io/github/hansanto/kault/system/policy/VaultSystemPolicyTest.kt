package io.github.hansanto.kault.system.policy

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.system.namespaces.response.NamespacesCreateResponse
import io.github.hansanto.kault.system.namespaces.response.NamespacesListResponse
import io.github.hansanto.kault.system.namespaces.response.NamespacesPatchResponse
import io.github.hansanto.kault.system.namespaces.response.NamespacesReadResponse
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.createVaultEnterpriseClient
import io.github.hansanto.kault.util.deleteAllNamespaces
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.replaceTemplateString
import io.github.hansanto.kault.util.waitUntilVaultAsyncOpCompleted
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotHaveLength
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class VaultSystemNamespacesTest :
    ShouldSpec({

        lateinit var client: VaultClient
        lateinit var policy: VaultSystemPolicy

        beforeTest {
            client = createVaultClient()
            policy = client.system.policy
        }

        afterTest {
            client.close()
        }

        should("use default path if not set in builder") {
            VaultSystemPolicyImpl.Default.PATH shouldBe "policy"

            val built = VaultSystemPolicyImpl.Companion(client.client, null) {
            }

            built.path shouldBe VaultSystemPolicyImpl.Default.PATH
        }

        should("use custom values in the builder") {
            val randomPath = randomString()
            val parentPath = randomString()

            val built = VaultSystemPolicyImpl.Companion(client.client, parentPath) {
                path = randomPath
            }

            built.path shouldBe "$parentPath/$randomPath"
        }

    })
