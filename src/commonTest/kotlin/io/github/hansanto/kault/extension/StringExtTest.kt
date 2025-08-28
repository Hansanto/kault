package io.github.hansanto.kault.extension

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.runTest
import kotlin.test.Test

class StringExtTest {

    @Test
    fun `should add url child path with empty parent and child`() = runTest {
        val parent = ""
        val child = ""
        val expected = ""
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    @Test
    fun `should add url child path with empty parent and non-empty child with leading slash`() = runTest {
        val parent = "/"
        val child = "/test/"
        val expected = "test"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    @Test
    fun `should add url child path with empty parent and non-empty child`() = runTest {
        val parent = ""
        val child = "v1"
        val expected = "v1"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    @Test
    fun `should add url child path with non-empty parent and empty child`() = runTest {
        val parent = "http://localhost:8200"
        val child = ""
        val expected = "http://localhost:8200/"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    @Test
    fun `should add url child path with non-empty parent and non-empty child`() = runTest {
        val parent = "http://localhost:8200"
        val child = "v1"
        val expected = "http://localhost:8200/v1"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    @Test
    fun `should add url child path with non-empty parent and non-empty child with leading slash`() = runTest {
        val parent = "http://localhost:8200"
        val child = "/v1"
        val expected = "http://localhost:8200/v1"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    @Test
    fun `should add url child path with non-empty parent and non-empty child with trailing slash`() = runTest {
        val parent = "http://localhost:8200"
        val child = "/v1/"
        val expected = "http://localhost:8200/v1"
        val actual = parent.addURLChildPath(child)
        actual shouldBe expected
    }

    @Test
    fun `should add url child path with non-empty parent and non-empty child with leading and trailing slash`() =
        runTest {
            val parent = "http://localhost:8200/"
            val child = "/v1/"
            val expected = "http://localhost:8200/v1"
            val actual = parent.addURLChildPath(child)
            actual shouldBe expected
        }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `should add url child path with non-empty parent and non-empty child with leading slash and no trailing slash`() =
        runTest {
            val parent = "http://localhost:8200/"
            val child = "/v1"
            val expected = "http://localhost:8200/v1"
            val actual = parent.addURLChildPath(child)
            actual shouldBe expected
        }

    @Suppress("ktlint:standard:max-line-length")
    @Test
    fun `should add url child path with non-empty parent and non-empty child with no leading slash and trailing slash`() =
        runTest {
            val parent = "http://localhost:8200"
            val child = "v1/"
            val expected = "http://localhost:8200/v1"
            val actual = parent.addURLChildPath(child)
            actual shouldBe expected
        }
}
