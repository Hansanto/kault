package io.github.hansanto.kault.auth.approle.payload

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class AppRoleGenerateSecretIDPayloadTest {

    @Test
    fun `should transform metadata empty map to empty json`() = runTest {
        val payload = AppRoleGenerateSecretIDPayload()
        payload.metadata(emptyMap())
        payload.metadata shouldBe "{}"
    }

    @Test
    fun `should transform metadata map with one entry to json`() = runTest {
        val payload = AppRoleGenerateSecretIDPayload()
        payload.metadata(mapOf("key" to "value"))
        payload.metadata shouldBe "{\"key\":\"value\"}"
    }

    @Test
    fun `should transform metadata from map with multiple entries`() = runTest {
        val payload = AppRoleGenerateSecretIDPayload()
        payload.metadata(mapOf("key1" to "value1", "key2" to "value2"))
        payload.metadata shouldBe "{\"key1\":\"value1\",\"key2\":\"value2\"}"
    }
}
