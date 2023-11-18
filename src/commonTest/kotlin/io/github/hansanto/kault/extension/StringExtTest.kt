package io.github.hansanto.kault.extension

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class StringExtTest : FunSpec({

    test("add url child path with empty parent and child") {
        val parent = ""
        val child = ""
        val expected = "/"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    test("add url child path with empty parent and non-empty child") {
        val parent = ""
        val child = "v1"
        val expected = "/v1"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    test("add url child path with non-empty parent and empty child") {
        val parent = "http://localhost:8200"
        val child = ""
        val expected = "http://localhost:8200/"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    test("add url child path with non-empty parent and non-empty child") {
        val parent = "http://localhost:8200"
        val child = "v1"
        val expected = "http://localhost:8200/v1"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    test("add url child path with non-empty parent and non-empty child with leading slash") {
        val parent = "http://localhost:8200"
        val child = "/v1"
        val expected = "http://localhost:8200/v1"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    test("add url child path with non-empty parent and non-empty child with trailing slash") {
        val parent = "http://localhost:8200"
        val child = "/v1/"
        val expected = "http://localhost:8200/v1"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    test("add url child path with non-empty parent and non-empty child with leading and trailing slash") {
        val parent = "http://localhost:8200/"
        val child = "/v1/"
        val expected = "http://localhost:8200/v1"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    test("add url child path with non-empty parent and non-empty child with leading slash and no trailing slash") {
        val parent = "http://localhost:8200/"
        val child = "/v1"
        val expected = "http://localhost:8200/v1"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    test("add url child path with non-empty parent and non-empty child with no leading slash and trailing slash") {
        val parent = "http://localhost:8200"
        val child = "v1/"
        val expected = "http://localhost:8200/v1"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }
})
