package io.github.hansanto.kault.system.policy

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.system.policy.VaultSystemPolicyTest.PolicyListCreate
import io.github.hansanto.kault.system.policy.response.PolicyListResponse
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.deleteAllPolicies
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.replaceTemplateString
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class VaultSystemPolicyTest :
    ShouldSpec({

        lateinit var client: VaultClient
        lateinit var policy: VaultSystemPolicy

        beforeTest {
            client = createVaultClient()
            policy = client.system.policy
        }

        afterTest {
            deleteAllPolicies(client)
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

        should("list return the default policies") {
            assertList(
                policy = policy,
                givenPath = null,
                expectedReadPath = "cases/system/policy/list/without-options/expected.json",
            )
        }

        should("list return the policies after creating a new policy") {
            assertList(
                policy = policy,
                givenPath = "cases/system/policy/list/with-options/given.json",
                expectedReadPath = "cases/system/policy/list/with-options/expected.json",
            )
        }
    }) {

    @Serializable
    data class PolicyListCreate(
        @SerialName("name")
        val name: String,
        @SerialName("policy")
        val policy: String,
    )
}

private suspend inline fun assertList(policy: VaultSystemPolicy, givenPath: String?, expectedReadPath: String,) {
    givenPath?.let {
        val given = readJson<PolicyListCreate>(it)
        policy.createOrUpdate(given.name, given.policy)
    }
    val actual = policy.list()
    val expected = replaceTemplateString(
        expected = readJson<PolicyListResponse>(expectedReadPath),
        response = actual,
    )
    actual.keys shouldContainExactlyInAnyOrder expected.keys
    actual.policies shouldContainExactlyInAnyOrder expected.policies
}
