package io.github.hansanto.kault.response

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe

class ResponseFieldsTest : ShouldSpec({

    should("have fields matching the response structure") {
        ResponseFields.DATA shouldBe "data"
        ResponseFields.AUTH shouldBe "auth"
    }
})
