package io.github.hansanto.kault.response

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class VaultResponseFieldTest : FunSpec({

    test("should have the correct fields") {
        VaultResponseField.DATA shouldBe "data"
        VaultResponseField.AUTH shouldBe "auth"
    }
})
