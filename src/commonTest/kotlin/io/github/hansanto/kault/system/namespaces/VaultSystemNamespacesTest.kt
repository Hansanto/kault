package io.github.hansanto.kault.system.namespaces

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.system.namespaces.response.NamespacesCreateResponse
import io.github.hansanto.kault.system.namespaces.response.NamespacesListResponse
import io.github.hansanto.kault.system.namespaces.response.NamespacesPatchResponse
import io.github.hansanto.kault.system.namespaces.response.NamespacesReadResponse
import io.github.hansanto.kault.util.createVaultEnterpriseClient
import io.github.hansanto.kault.util.deleteAllNamespaces
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.replaceTemplateString
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class VaultSystemNamespacesTest :
    ShouldSpec({

        lateinit var client: VaultClient
        lateinit var namespaces: VaultSystemNamespaces

        beforeTest {
            client = createVaultEnterpriseClient()
            namespaces = client.system.namespaces
        }

        afterTest {
            deleteAllNamespaces(client)
            client.close()
        }

        should("use default path if not set in builder") {
            VaultSystemNamespacesImpl.Default.PATH shouldBe "namespaces"

            val built = VaultSystemNamespacesImpl.Companion(client.client, null) {
            }

            built.path shouldBe VaultSystemNamespacesImpl.Default.PATH
        }

        should("use custom values in the builder") {
            val randomPath = randomString()
            val parentPath = randomString()

            val built = VaultSystemNamespacesImpl.Companion(client.client, parentPath) {
                path = randomPath
            }

            built.path shouldBe "$parentPath/$randomPath"
        }

        should("throw exception when listing the namespaces when no namespace is set") {
            shouldThrow<VaultAPIException> {
                namespaces.list()
            }
        }

        should("list with created namespaces without options") {
            assertListNamespaces(
                namespaces = namespaces,
                givenPath = "cases/sys/namespaces/list/without-options/given.json",
                expectedReadPath = "cases/sys/namespaces/list/without-options/expected.json",
            )
        }

        should("list with created namespaces with options") {
            assertListNamespaces(
                namespaces = namespaces,
                givenPath = "cases/sys/namespaces/list/with-options/given.json",
                expectedReadPath = "cases/sys/namespaces/list/with-options/expected.json",
            )
        }

        should("create a namespace without options") {
            assertCreate(
                path = "test-namespace",
                namespaces = namespaces,
                givenPath = null,
                expectedCreatePath = "cases/sys/namespaces/create/without-options/expected.json",
                expectedReadPath = "cases/sys/namespaces/create/without-options/expected.json"
            )
        }

        should("create a namespace with options") {
            assertCreate(
                path = "test-namespace",
                namespaces = namespaces,
                givenPath = "cases/sys/namespaces/create/with-options/given.json",
                expectedCreatePath = "cases/sys/namespaces/create/with-options/expected.json",
                expectedReadPath = "cases/sys/namespaces/create/with-options/expected.json"
            )
        }

        should("patch a namespace without options") {
            assertPatch(
                path = "test-namespace",
                namespaces = namespaces,
                givenCreatePath = "cases/sys/namespaces/patch/without-options/given_create.json",
                givenPatchPath = "cases/sys/namespaces/patch/without-options/given_patch.json",
                expectedPatchPath = "cases/sys/namespaces/patch/without-options/expected.json",
                expectedReadPath = "cases/sys/namespaces/patch/without-options/expected.json"
            )
        }

        should("patch a namespace with options") {
            assertPatch(
                path = "test-namespace",
                namespaces = namespaces,
                givenCreatePath = "cases/sys/namespaces/patch/with-options/given_create.json",
                givenPatchPath = "cases/sys/namespaces/patch/with-options/given_patch.json",
                expectedPatchPath = "cases/sys/namespaces/patch/with-options/expected.json",
                expectedReadPath = "cases/sys/namespaces/patch/with-options/expected.json"
            )
        }
    }) {

    @Serializable
    data class NamespacesListCreate(
        @SerialName("path")
        val path: String,
        @SerialName("custom_metadata")
        val customMetadata: Map<String, String>? = null,
    )
}

private suspend inline fun assertListNamespaces(
    namespaces: VaultSystemNamespaces,
    givenPath: String,
    expectedReadPath: String,
) {
    val given = readJson<List<VaultSystemNamespacesTest.NamespacesListCreate>>(givenPath)
    given.forEach {
        namespaces.create(
            path = it.path,
            customMetadata = it.customMetadata,
        )
    }

    val actual = namespaces.list()
    val expected = replaceTemplateString(
        expected = readJson<NamespacesListResponse>(expectedReadPath),
        response = actual,
    )
    actual.keyInfo shouldBe expected.keyInfo
    actual.keys shouldContainExactlyInAnyOrder expected.keys
}

private suspend fun assertCreate(
    path: String,
    namespaces: VaultSystemNamespaces,
    givenPath: String?,
    expectedCreatePath: String,
    expectedReadPath: String
) {
    assertCreate(
        path,
        namespaces,
        givenPath,
        expectedCreatePath,
        expectedReadPath
    ) { payload ->
        namespaces.create(path, payload)
    }
}

private suspend inline fun assertCreate(
    path: String,
    namespaces: VaultSystemNamespaces,
    givenPath: String?,
    expectedCreatePath: String,
    expectedReadPath: String,
    create: (Map<String, String>?) -> NamespacesCreateResponse
) {
    val given = givenPath?.let { readJson<Map<String, String>>(it) }
    val result = create(given)
    result shouldBe replaceTemplateString(
        expected = readJson<NamespacesCreateResponse>(expectedCreatePath),
        response = result,
    )

    val actual = namespaces.read(path)
    actual shouldBe replaceTemplateString(
        expected = readJson<NamespacesReadResponse>(expectedReadPath),
        response = actual,
    )
    result.id shouldBe actual.id
    result.uuid shouldBe actual.uuid
}

private suspend fun assertPatch(
    path: String,
    namespaces: VaultSystemNamespaces,
    givenCreatePath: String,
    givenPatchPath: String,
    expectedPatchPath: String,
    expectedReadPath: String
) {
    assertPatch(
        path,
        namespaces,
        givenCreatePath,
        givenPatchPath,
        expectedPatchPath,
        expectedReadPath
    ) { payload ->
        namespaces.patch(path, payload)
    }
}

private suspend inline fun assertPatch(
    path: String,
    namespaces: VaultSystemNamespaces,
    givenCreatePath: String,
    givenPatchPath: String,
    expectedPatchPath: String,
    expectedReadPath: String,
    patch: (Map<String, String>) -> NamespacesPatchResponse
) {
    val createGiven = readJson<Map<String, String>>(givenCreatePath)
    val created = namespaces.create(path, createGiven)

    val given = readJson<Map<String, String>>(givenPatchPath)
    val result = patch(given)
    result shouldBe replaceTemplateString(
        expected = readJson<NamespacesReadResponse>(expectedPatchPath),
        response = result,
    )

    val actual = namespaces.read(path)
    actual shouldBe replaceTemplateString(
        expected = readJson<NamespacesReadResponse>(expectedReadPath),
        response = actual,
    )
    created.id shouldBe actual.id
    created.uuid shouldBe actual.uuid
}
