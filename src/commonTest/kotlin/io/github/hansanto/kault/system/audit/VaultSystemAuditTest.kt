package io.github.hansanto.kault.system.audit

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.system.audit.payload.AuditingEnableDevicePayload
import io.github.hansanto.kault.system.audit.response.AuditingDeviceResponse
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
// TODO
class VaultSystemAuditTest : FunSpec({

    lateinit var client: VaultClient
    lateinit var audit: VaultSystemAudit

    beforeSpec {
        client = createVaultClient()
        audit = client.system.audit
    }

    beforeTest {
        audit.list().forEach { audit.disable(it.key) }
    }

    afterSpec {
        client.close()
    }

    test("builder default variables should be set correctly") {
        VaultSystemAuditImpl.Default.PATH shouldBe "audit"

        val built = VaultSystemAuditImpl(client.client, null) {
        }

        built.path shouldBe VaultSystemAuditImpl.Default.PATH
    }

    test("builder should set values correctly") {
        val randomPath = randomString()
        val parentPath = randomString()

        val built = VaultSystemAuditImpl(client.client, parentPath) {
            path = randomPath
        }

        built.path shouldBe "$parentPath/$randomPath"
    }

    test("list without audit enabled") {
        val response = audit.list()
        response.size shouldBe 0
    }

    test("list with audit enabled without options") {
        assertListWithEnabledAudit(
            audit,
            listOf(
                "cases/sys/audit/without_options/given.json"
            ),
            "cases/sys/audit/without_options/expected.json"
        )
    }

    test("list with audit enabled with options") {
        assertListWithEnabledAudit(
            audit,
            listOf(
                "cases/sys/audit/with_options/given.json"
            ),
            "cases/sys/audit/with_options/expected.json"
        )
    }

    test("list with several audit enabled") {
        assertListWithEnabledAudit(
            audit,
            listOf(
                "cases/sys/audit/several_audit/given1.json",
                "cases/sys/audit/several_audit/given2.json"
            ),
            "cases/sys/audit/several_audit/expected.json"
        )
    }

    test("disable with non existing audit") {
        val response = audit.disable("non-existing-audit")
        response shouldBe true
    }

    test("disable with existing audit") {
        val given = readJson<AuditingEnableDevicePayload>("cases/sys/audit/without_options/given.json")
        val path = "role"
        audit.enable(path, given) shouldBe true

        val response = audit.disable(path)
        response shouldBe true

        val list = audit.list()
        list.size shouldBe 0
    }
})

private suspend fun assertListWithEnabledAudit(
    audit: VaultSystemAudit,
    givenPaths: List<String>,
    expectedPath: String
) {
    givenPaths.forEachIndexed { index, value ->
        val given = readJson<AuditingEnableDevicePayload>(value)
        audit.enable("role-test$index", given) shouldBe true
    }

    val response = audit.list()
    val expected = readJson<Map<String, AuditingDeviceResponse>>(expectedPath)
    response shouldBe expected
}
