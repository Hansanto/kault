package com.github.hansanto.kault.system.audit

import com.github.hansanto.kault.VaultClient
import com.github.hansanto.kault.system.audit.payload.AuditingEnableDevicePayload
import com.github.hansanto.kault.util.createVaultClient
import com.github.hansanto.kault.util.readJson
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VaultSystemAuditTest : FunSpec({

    lateinit var client: VaultClient
    lateinit var auth: VaultSystemAudit

    beforeSpec {
        client = createVaultClient()
        auth = client.system.audit
    }

    afterSpec {
        client.close()
    }

    test("list without audit enabled") {
        val result = auth.list()
    }

    test("list with audit enabled without options") {
        auth.enable("file") {
            type = "file"
            options = mapOf("file_path" to "/tmp/audit.log")
        } shouldBe true
        val result = auth.list()
    }

    test("list with audit enabled with options") {
        val given = readJson<AuditingEnableDevicePayload>("cases/sys/audit/list/with_additional/given.json")
        auth.enable("file", given) shouldBe true
        val result = auth.list()
    }

})
