package io.github.hansanto.kault.extension

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class HttpExtTest {

    @Test
    fun `should check that URL_PATH_SEPARATOR is slash`() = runTest {
        URL_PATH_SEPARATOR shouldBe "/"
    }
}
