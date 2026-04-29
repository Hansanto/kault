package io.github.hansanto.kault.system.policy

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.system.policy.VaultSystemPolicyTest.PolicyCreate
import io.github.hansanto.kault.system.policy.response.PolicyListResponse
import io.github.hansanto.kault.system.policy.response.PolicyReadResponse
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.deleteAllPolicies
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.readString
import io.github.hansanto.kault.util.replaceTemplateString
import io.kotest.assertions.throwables.shouldThrow
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

        should("create a policy with options") {
            assertCreate(
                policy = policy,
                givenPath = "cases/system/policy/create/with-options/given.json",
                expectedReadPath = "cases/system/policy/create/with-options/expected.json"
            )
        }

        should("update a policy with options") {
            assertUpdate(
                policy = policy,
                givenCreatePath = "cases/system/policy/update/with-options/given_create.json",
                givenPatchPath = "cases/system/policy/update/with-options/given_update.hcl",
                expectedReadPath = "cases/system/policy/update/with-options/expected.json"
            )
        }

        should("delete a non-existing policy should return false") {
            val name = randomString()
            policy.delete(name) shouldBe true
        }

        should("delete an existing policy") {
            val given = readJson<PolicyCreate>("cases/system/policy/create/with-options/given.json")
            val name = given.name
            val policyString = given.policy

            policy.createOrUpdate(name, policyString) shouldBe true
            policy.delete(name) shouldBe true
            shouldThrow<VaultAPIException> {
                policy.read(name)
            }
        }
    }) {

    @Serializable
    data class PolicyCreate(
        @SerialName("name")
        val name: String,
        @SerialName("policy")
        val policy: String,
    )
}

private suspend inline fun assertList(policy: VaultSystemPolicy, givenPath: String?, expectedReadPath: String) {
    givenPath?.let {
        val given = readJson<PolicyCreate>(it)
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

private suspend fun assertCreate(policy: VaultSystemPolicy, givenPath: String, expectedReadPath: String) {
    val given = readJson<PolicyCreate>(givenPath)
    val name = given.name
    val policyString = given.policy

    policy.createOrUpdate(name, policyString) shouldBe true

    val actual = policy.read(name)
    replaceTemplateString(
        expected = readJson<PolicyReadResponse>(expectedReadPath),
        response = actual,
    ) shouldBe actual
}

private suspend fun assertUpdate(
    policy: VaultSystemPolicy,
    givenCreatePath: String,
    givenPatchPath: String,
    expectedReadPath: String
) {
    val givenCreate = readJson<PolicyCreate>(givenCreatePath)
    val givenName = givenCreate.name
    val givenPolicy = givenCreate.policy

    policy.createOrUpdate(givenName, givenPolicy) shouldBe true

    val updatePolicy = readString(givenPatchPath)
    policy.createOrUpdate(givenName, updatePolicy) shouldBe true

    val actual = policy.read(givenName)
    replaceTemplateString(
        expected = readJson<PolicyReadResponse>(expectedReadPath),
        response = actual,
    ) shouldBe actual
}
