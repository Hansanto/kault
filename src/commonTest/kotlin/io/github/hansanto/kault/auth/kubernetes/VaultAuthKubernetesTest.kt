package io.github.hansanto.kault.auth.kubernetes

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.kubernetes.payload.KubernetesLoginPayload
import io.github.hansanto.kault.auth.kubernetes.payload.KubernetesWriteAuthRolePayload
import io.github.hansanto.kault.auth.kubernetes.response.KubernetesConfigureAuthResponse
import io.github.hansanto.kault.auth.kubernetes.response.KubernetesReadAuthRoleResponse
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.system.auth.enable
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.getKubernetesCaCert
import io.github.hansanto.kault.util.getKubernetesHost
import io.github.hansanto.kault.util.getKubernetesToken
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

private const val DEFAULT_ROLE_NAME = "test"

class VaultAuthKubernetesTest : FunSpec({

    lateinit var client: VaultClient
    lateinit var kubernetes: VaultAuthKubernetes

    lateinit var kubernetesHost: String
    lateinit var kubernetesCaCert: String

    beforeSpec {
        client = createVaultClient()
        kubernetes = client.auth.kubernetes
        kubernetesHost = getKubernetesHost()
        kubernetesCaCert = getKubernetesCaCert()

        runCatching {
            client.system.auth.enable("kubernetes") {
                type = "kubernetes"
            }
        }
    }

    beforeTest {
        kubernetes.configure {
            this.kubernetesHost = kubernetesHost
            this.kubernetesCaCert = kubernetesCaCert
            tokenReviewerJwt = getKubernetesToken()
        }

        runCatching {
            kubernetes.list().forEach {
                kubernetes.delete(it) shouldBe true
            }
        }

        shouldThrow<VaultAPIException> { kubernetes.read(DEFAULT_ROLE_NAME) }
    }

    afterSpec {
        client.close()
    }

    test("builder default variables should be set correctly") {
        VaultAuthKubernetesImpl.Default.PATH shouldBe "kubernetes"

        val built = VaultAuthKubernetesImpl(client.client, null) {
        }

        built.path shouldBe VaultAuthKubernetesImpl.Default.PATH
    }

    test("builder should set values correctly") {
        val builderPath = randomString()
        val parentPath = randomString()

        val built = VaultAuthKubernetesImpl(client.client, parentPath) {
            path = builderPath
        }

        built.path shouldBe "$parentPath/$builderPath"
    }

    test("read default configuration") {
        kubernetes.readConfiguration() shouldBe KubernetesConfigureAuthResponse(
            kubernetesHost = kubernetesHost,
            kubernetesCaCert = kubernetesCaCert,
            pemKeys = emptyList(),
            disableLocalCaJwt = false
        )
    }

    test("create without options") {
        assertCreate(
            kubernetes,
            "cases/auth/kubernetes/create/without_options/given.json",
            "cases/auth/kubernetes/create/without_options/expected.json"
        )
    }

    test("create with options") {
        assertCreate(
            kubernetes,
            "cases/auth/kubernetes/create/with_options/given.json",
            "cases/auth/kubernetes/create/with_options/expected.json"
        )
    }

    test("list with no roles") {
        shouldThrow<VaultAPIException> {
            kubernetes.list()
        }
    }

    test("list with roles") {
        val roles = List(10) { "test-$it" }
        roles.forEach { createRole(kubernetes, it) }
        kubernetes.list() shouldContainExactlyInAnyOrder roles
    }

    test("delete non-existing role") {
        shouldThrow<VaultAPIException> { kubernetes.read(DEFAULT_ROLE_NAME) }
        kubernetes.delete(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { kubernetes.read(DEFAULT_ROLE_NAME) }
    }

    test("delete existing role") {
        createRole(kubernetes, DEFAULT_ROLE_NAME)
        shouldNotThrow<VaultAPIException> { kubernetes.read(DEFAULT_ROLE_NAME) }
        kubernetes.delete(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { kubernetes.read(DEFAULT_ROLE_NAME) }
    }

    test("login with non-existing role") {
        shouldThrow<VaultAPIException> { kubernetes.login(KubernetesLoginPayload(DEFAULT_ROLE_NAME, "x")) }
    }

    test("login with existing role") {
        kubernetes.createOrUpdate(DEFAULT_ROLE_NAME) {
            boundServiceAccountNames = listOf("*")
            boundServiceAccountNamespaces = listOf("*")
        } shouldBe true
        val response = kubernetes.login(KubernetesLoginPayload(DEFAULT_ROLE_NAME, getKubernetesToken()))
    }
})

private suspend fun createRole(
    kubernetes: VaultAuthKubernetes,
    role: String
) {
    kubernetes.createOrUpdate(role) {
        boundServiceAccountNames = listOf("*")
        boundServiceAccountNamespaces = listOf("*")
    } shouldBe true
}

private suspend fun assertCreate(
    kubernetes: VaultAuthKubernetes,
    givenPath: String,
    expectedReadPath: String
) {
    val given = readJson<KubernetesWriteAuthRolePayload>(givenPath)
    kubernetes.createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true

    val response = kubernetes.read(DEFAULT_ROLE_NAME)
    val expected = readJson<KubernetesReadAuthRoleResponse>(expectedReadPath)
    response shouldBe expected
}
