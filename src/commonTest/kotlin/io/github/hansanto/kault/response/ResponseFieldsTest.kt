package io.github.hansanto.kault.response

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class ResponseFieldsTest : FunSpec({

    test("should have the correct fields") {
        ResponseFields.DATA shouldBe "data"
        ResponseFields.AUTH shouldBe "auth"
    }
})
