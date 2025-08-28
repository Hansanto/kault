package io.github.hansanto.kault.system.audit.payload

import io.github.hansanto.kault.system.audit.common.AuditType
import io.github.hansanto.kault.util.randomBoolean
import io.github.hansanto.kault.util.randomString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AuditingEnableDevicePayloadTest {

    @Test
    fun `should throw exception when build builder without 'type' field`() = runTest {
        val builder = AuditingEnableDevicePayload.Builder().apply {
            description = "description"
            local = true
            options = mapOf("key" to "value")
        }

        shouldThrow<Exception> {
            builder.build()
        }
    }

    @Test
    fun `should build builder with only 'type' field`() = runTest {
        val randomType = AuditType.entries.random()

        val payload = AuditingEnableDevicePayload.Builder().apply {
            type = randomType
        }.build()

        payload.type shouldBe randomType
        payload.description shouldBe null
        payload.local shouldBe null
        payload.options shouldBe null
    }

    @Test
    fun `should build builder with all fields set`() = runTest {
        val randomType = AuditType.entries.random()
        val randomDescription = randomString()
        val randomLocal = randomBoolean()
        val randomOptions = mapOf(randomString() to randomString())

        val payload = AuditingEnableDevicePayload.Builder().apply {
            type = randomType
            description = randomDescription
            local = randomLocal
            options = randomOptions
        }.build()

        payload.type shouldBe randomType
        payload.description shouldBe randomDescription
        payload.local shouldBe randomLocal
        payload.options shouldBe randomOptions
    }
}
