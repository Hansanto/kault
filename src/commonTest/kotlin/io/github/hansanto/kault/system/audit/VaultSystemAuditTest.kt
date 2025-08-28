package io.github.hansanto.kault.system.audit

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.system.audit.payload.AuditingEnableDevicePayload
import io.github.hansanto.kault.system.audit.response.AuditingDeviceResponse
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.disableAllAudit
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

class VaultSystemAuditTest {

    lateinit var client: VaultClient
    lateinit var audit: VaultSystemAudit

    @BeforeTest
    fun onBefore() = runTest {
        client = createVaultClient()
        audit = client.system.audit
    }

    @AfterTest
    fun onAfter() = runTest {
        disableAllAudit(client)
        client.close()
    }

    @Test
    fun `should use default path if not set in builder`() = runTest {
        VaultSystemAuditImpl.Default.PATH shouldBe "audit"

        val built = VaultSystemAuditImpl(client.client, null) {
        }

        built.path shouldBe VaultSystemAuditImpl.Default.PATH
    }

    @Test
    fun `should use custom values in the builder`() = runTest {
        val randomPath = randomString()
        val parentPath = randomString()

        val built = VaultSystemAuditImpl(client.client, parentPath) {
            path = randomPath
        }

        built.path shouldBe "$parentPath/$randomPath"
    }

    @Test
    fun `should list without audit enabled`() = runTest {
        val response = audit.list()
        response.size shouldBe 0
    }

    @Test
    fun `should list with audit enabled with default values`() = runTest {
        assertListWithEnabledAudit(
            audit,
            listOf(
                "cases/sys/audit/without_options/given.json"
            ),
            "cases/sys/audit/without_options/expected.json"
        )
    }

    @Test
    fun `should list with audit enabled with all defined values`() = runTest {
        assertListWithEnabledAudit(
            audit,
            listOf(
                "cases/sys/audit/with_options/given.json"
            ),
            "cases/sys/audit/with_options/expected.json"
        )
    }

    @Test
    fun `should list with several audit enabled`() = runTest {
        assertListWithEnabledAudit(
            audit,
            listOf(
                "cases/sys/audit/several_audit/given1.json",
                "cases/sys/audit/several_audit/given2.json"
            ),
            "cases/sys/audit/several_audit/expected.json"
        )
    }

    @Test
    fun `should list using builder with audit enabled with all defined values`() = runTest {
        assertListWithEnabledAuditWithBuilder(
            audit,
            listOf(
                "cases/sys/audit/with_options/given.json"
            ),
            "cases/sys/audit/with_options/expected.json"
        )
    }

    @Test
    fun `should list using builder with several audit enabled`() = runTest {
        assertListWithEnabledAuditWithBuilder(
            audit,
            listOf(
                "cases/sys/audit/several_audit/given1.json",
                "cases/sys/audit/several_audit/given2.json"
            ),
            "cases/sys/audit/several_audit/expected.json"
        )
    }

    @Test
    fun `should disable with non-existing audit`() = runTest {
        val response = audit.disable("non-existing-audit")
        response shouldBe true
    }

    @Test
    fun `should disable with existing audit`() = runTest {
        val given = readJson<AuditingEnableDevicePayload>("cases/sys/audit/without_options/given.json")
        val path = "role"
        audit.enable(path, given) shouldBe true

        val response = audit.disable(path)
        response shouldBe true

        val list = audit.list()
        list.size shouldBe 0
    }
}

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
