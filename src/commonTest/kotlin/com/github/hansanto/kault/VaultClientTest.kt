package com.github.hansanto.kault

import io.kotest.matchers.shouldBe
import kotlin.test.Test

class VaultClientTest {

    @Test
    fun shouldDoSomething() {
        VaultClient().doSomething() shouldBe "VaultClient"
    }
}
