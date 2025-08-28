package io.github.hansanto.kault.response

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class ResponseFieldsTest {

    @Test
    fun `should have fields matching the response structure`() = runTest {
        ResponseFields.DATA shouldBe "data"
        ResponseFields.AUTH shouldBe "auth"
    }
}
