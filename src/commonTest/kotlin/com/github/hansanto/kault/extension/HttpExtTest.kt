package com.github.hansanto.kault.extension

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class HttpExtTest : FunSpec({

    test("url separator should be trailing slash") {
        URL_PATH_SEPARATOR shouldBe "/"
    }
})
