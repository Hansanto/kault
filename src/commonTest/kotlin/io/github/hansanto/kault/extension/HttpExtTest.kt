package io.github.hansanto.kault.extension

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class HttpExtTest : ShouldSpec({

    should("check that URL_PATH_SEPARATOR is /") {
        URL_PATH_SEPARATOR shouldBe "/"
    }
})
