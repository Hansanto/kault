package com.github.hansanto.kault.system.audit

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.system.audit.payload.AuditingEnableDevicePayload
import com.github.hansanto.kault.system.audit.response.AuditingDeviceResponse
import com.github.hansanto.kault.util.createVaultClient
import com.github.hansanto.kault.util.readJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

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

    test("list without audit enabled") {
        val response = audit.list()
        response.size shouldBe 0
    }

    test("list with audit enabled without options") {
        assertListWithEnabledAudit(
            audit,
            listOf(
                "cases/sys/audit/without_options/given.json",
            ),
            "cases/sys/audit/without_options/expected.json"
        )
    }

    test("list with audit enabled with options") {
        assertListWithEnabledAudit(
            audit,
            listOf(
                "cases/sys/audit/with_options/given.json",
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
