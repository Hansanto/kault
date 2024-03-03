package io.github.hansanto.kault.auth.kubernetes

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.system.auth.enable
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.getKubernetesCaCert
import io.github.hansanto.kault.util.getKubernetesHost
import io.github.hansanto.kault.util.randomString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

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

    test("should set configuration") {
        val response = kubernetes.configure {
            this.kubernetesHost = kubernetesHost
            this.kubernetesCaCert = kubernetesCaCert
        }

        response shouldBe true
    }
})
