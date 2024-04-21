package io.github.hansanto.kault.system.audit

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.system.audit.payload.AuditingEnableDevicePayload
import io.github.hansanto.kault.system.audit.response.AuditingDeviceResponse
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.disableAllAudit
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class VaultSystemAuditTest : ShouldSpec({

    lateinit var client: VaultClient
    lateinit var audit: VaultSystemAudit

    beforeTest {
        client = createVaultClient()
        audit = client.system.audit
        disableAllAudit(client)
    }

    afterTest {
        client.close()
    }

    should("use default path if not set in builder") {
        VaultSystemAuditImpl.Default.PATH shouldBe "audit"

        val built = VaultSystemAuditImpl(client.client, null) {
        }

        built.path shouldBe VaultSystemAuditImpl.Default.PATH
    }

    should("use custom values in the builder") {
        val randomPath = randomString()
        val parentPath = randomString()

        val built = VaultSystemAuditImpl(client.client, parentPath) {
            path = randomPath
        }

        built.path shouldBe "$parentPath/$randomPath"
    }

    should("list without audit enabled") {
        val response = audit.list()
        response.size shouldBe 0
    }

    should("list with audit enabled with default values") {
        assertListWithEnabledAudit(
            audit,
            listOf(
                "cases/sys/audit/without_options/given.json"
            ),
            "cases/sys/audit/without_options/expected.json"
        )
    }

    should("list with audit enabled with all defined values") {
        assertListWithEnabledAudit(
            audit,
            listOf(
                "cases/sys/audit/with_options/given.json"
            ),
            "cases/sys/audit/with_options/expected.json"
        )
    }

    should("list with several audit enabled") {
        assertListWithEnabledAudit(
            audit,
            listOf(
                "cases/sys/audit/several_audit/given1.json",
                "cases/sys/audit/several_audit/given2.json"
            ),
            "cases/sys/audit/several_audit/expected.json"
        )
    }

    should("list using builder with audit enabled with all defined values") {
        assertListWithEnabledAuditWithBuilder(
            audit,
            listOf(
                "cases/sys/audit/with_options/given.json"
            ),
            "cases/sys/audit/with_options/expected.json"
        )
    }

    should("list using builder with several audit enabled") {
        assertListWithEnabledAuditWithBuilder(
            audit,
            listOf(
                "cases/sys/audit/several_audit/given1.json",
                "cases/sys/audit/several_audit/given2.json"
            ),
            "cases/sys/audit/several_audit/expected.json"
        )
    }

    should("disable with non-existing audit") {
        val response = audit.disable("non-existing-audit")
        response shouldBe true
    }

    should("disable with existing audit") {
        val given = readJson<AuditingEnableDevicePayload>("cases/sys/audit/without_options/given.json")
        val path = "role"
        audit.enable(path, given) shouldBe true

        val response = audit.disable(path)
        response shouldBe true

        val list = audit.list()
        list.size shouldBe 0
    }
})

private suspend inline fun assertListWithEnabledAudit(
    audit: VaultSystemAudit,
    givenPaths: List<String>,
    expectedPath: String
) {
    assertListWithEnabledAudit(audit, givenPaths, expectedPath) { path, payload ->
        audit.enable(path, payload)
    }
}

private suspend inline fun assertListWithEnabledAuditWithBuilder(
    audit: VaultSystemAudit,
    givenPaths: List<String>,
    expectedPath: String
) {
    assertListWithEnabledAudit(audit, givenPaths, expectedPath) { path, payload ->
        audit.enable(path) {
            type = payload.type
            description = payload.description
            local = payload.local
            options = payload.options
        }
    }
}

private suspend inline fun assertListWithEnabledAudit(
    audit: VaultSystemAudit,
    givenPaths: List<String>,
    expectedPath: String,
    enableAudit: (String, AuditingEnableDevicePayload) -> Boolean
) {
    givenPaths.forEachIndexed { index, value ->
        val given = readJson<AuditingEnableDevicePayload>(value)
        enableAudit("role-test$index", given) shouldBe true
    }

    val response = audit.list()
    val expected = readJson<Map<String, AuditingDeviceResponse>>(expectedPath)
    response shouldBe expected
}
