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
        val given = readJson<AuditingEnableDevicePayload>("cases/sys/audit/list/without_options/given.json")
        audit.enable("role-test", given) shouldBe true

        val response = audit.list()
        val expected = readJson<Map<String, AuditingDeviceResponse>>("cases/sys/audit/list/without_options/expected.json")
        response shouldBe expected
    }

    test("list with audit enabled with options") {
        val given = readJson<AuditingEnableDevicePayload>("cases/sys/audit/list/with_options/given.json")
        audit.enable("role-test", given) shouldBe true

        val response = audit.list()
        val expected = readJson<Map<String, AuditingDeviceResponse>>("cases/sys/audit/list/with_options/expected.json")
        response shouldBe expected
    }
})
