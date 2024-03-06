package io.github.hansanto.kault.auth.kubernetes

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.kubernetes.payload.KubernetesWriteAuthRolePayload
import io.github.hansanto.kault.auth.kubernetes.response.KubernetesConfigureAuthResponse
import io.github.hansanto.kault.auth.kubernetes.response.KubernetesReadAuthRoleResponse
import io.github.hansanto.kault.system.auth.enable
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.getKubernetesCaCert
import io.github.hansanto.kault.util.getKubernetesHost
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.core.spec.style.FunSpec
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
        }
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

    test("create with options") {
        assertCreate(
            kubernetes,
            "cases/auth/kubernetes/create/with_options/given.json",
            "cases/auth/kubernetes/create/with_options/expected.json"
        )
    }
})

private suspend fun assertCreate(
    kubernetes: VaultAuthKubernetes,
    givenPath: String,
    expectedReadPath: String
) {
    val given = readJson<KubernetesWriteAuthRolePayload>(givenPath)
    kubernetes.createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true

    val response = kubernetes.read(DEFAULT_ROLE_NAME)
    println(response)
    val expected = readJson<KubernetesReadAuthRoleResponse>(expectedReadPath)
    response shouldBe expected
}
