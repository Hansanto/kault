package io.github.hansanto.kault.auth.kubernetes

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.common.response.LoginResponse
import io.github.hansanto.kault.auth.kubernetes.payload.KubernetesLoginPayload
import io.github.hansanto.kault.auth.kubernetes.payload.KubernetesWriteAuthRolePayload
import io.github.hansanto.kault.auth.kubernetes.response.KubernetesConfigureAuthResponse
import io.github.hansanto.kault.auth.kubernetes.response.KubernetesReadAuthRoleResponse
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.system.auth.enable
import io.github.hansanto.kault.util.DEFAULT_ROLE_NAME
import io.github.hansanto.kault.util.KubernetesUtil
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class VaultAuthKubernetesTest : ShouldSpec({

    lateinit var client: VaultClient
    lateinit var kubernetes: VaultAuthKubernetes

    beforeSpec {
        client = createVaultClient()
        kubernetes = client.auth.kubernetes

        runCatching {
            client.system.auth.enable("kubernetes") {
                type = "kubernetes"
            }
        }
    }

    afterSpec {
        client.close()
    }

    beforeTest {
        kubernetes.configure {
            kubernetesHost = KubernetesUtil.host
            kubernetesCaCert = KubernetesUtil.caCert
            tokenReviewerJwt = KubernetesUtil.token
        }

        runCatching {
            kubernetes.list().forEach {
                kubernetes.deleteRole(it) shouldBe true
            }
        }

        shouldThrow<VaultAPIException> { kubernetes.readRole(DEFAULT_ROLE_NAME) }
    }

    should("use default path if not set in builder") {
        VaultAuthKubernetesImpl.Default.PATH shouldBe "kubernetes"

        val built = VaultAuthKubernetesImpl(client.client, null) {
        }

        built.path shouldBe VaultAuthKubernetesImpl.Default.PATH
    }

    should("use custom path if set in builder") {
        val builderPath = randomString()
        val parentPath = randomString()

        val built = VaultAuthKubernetesImpl(client.client, parentPath) {
            path = builderPath
        }

        built.path shouldBe "$parentPath/$builderPath"
    }

    should("read default configuration") {
        kubernetes.readConfiguration() shouldBe KubernetesConfigureAuthResponse(
            kubernetesHost = KubernetesUtil.host,
            kubernetesCaCert = KubernetesUtil.caCert,
            pemKeys = emptyList(),
            disableLocalCaJwt = false
        )
    }

    should("create a role with default values") {
        assertCreateOrUpdateRole(
            kubernetes,
            "cases/auth/kubernetes/create/without_options/given.json",
            "cases/auth/kubernetes/create/without_options/expected.json"
        )
    }

    should("create a role with all defined values") {
        assertCreateOrUpdateRole(
            kubernetes,
            "cases/auth/kubernetes/create/with_options/given.json",
            "cases/auth/kubernetes/create/with_options/expected.json"
        )
    }

    should("throw exception if no role was created when listing roles") {
        shouldThrow<VaultAPIException> {
            kubernetes.list()
        }
    }

    should("return created roles when listing") {
        val roles = List(10) { "test-$it" }
        roles.forEach { createRole(kubernetes, it) }
        kubernetes.list() shouldContainExactlyInAnyOrder roles
    }

    should("do nothing when deleting non-existing role") {
        shouldThrow<VaultAPIException> { kubernetes.readRole(DEFAULT_ROLE_NAME) }
        kubernetes.deleteRole(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { kubernetes.readRole(DEFAULT_ROLE_NAME) }
    }

    should("delete existing role") {
        createRole(kubernetes, DEFAULT_ROLE_NAME)
        shouldNotThrow<VaultAPIException> { kubernetes.readRole(DEFAULT_ROLE_NAME) }
        kubernetes.deleteRole(DEFAULT_ROLE_NAME) shouldBe true
        shouldThrow<VaultAPIException> { kubernetes.readRole(DEFAULT_ROLE_NAME) }
    }

    should("throw exception when login with non-existing role") {
        shouldThrow<VaultAPIException> {
            kubernetes.login(
                KubernetesLoginPayload(
                    DEFAULT_ROLE_NAME,
                    KubernetesUtil.token
                )
            )
        }
    }

    should("throw exception when login with invalid token") {
        createRole(kubernetes, DEFAULT_ROLE_NAME)
        shouldThrow<VaultAPIException> { kubernetes.login(KubernetesLoginPayload(DEFAULT_ROLE_NAME, "invalid-token")) }
    }

    should("login with valid token") {
        createRole(kubernetes, DEFAULT_ROLE_NAME)

        val response = kubernetes.login(KubernetesLoginPayload(DEFAULT_ROLE_NAME, KubernetesUtil.token))
        val expected = readJson<LoginResponse>("cases/auth/kubernetes/login/expected.json").run {
            copy(
                clientToken = response.clientToken,
                accessor = response.accessor,
                entityId = response.entityId,
                leaseDuration = response.leaseDuration,
                metadata = metadata.toMutableMap().apply {
                    put("service_account_uid", response.metadata["service_account_uid"]!!)
                }
            )
        }

        response shouldBe expected
    }
})

private suspend fun createRole(
    kubernetes: VaultAuthKubernetes,
    role: String
) {
    kubernetes.createOrUpdateRole(role) {
        boundServiceAccountNames = listOf("*")
        boundServiceAccountNamespaces = listOf("*")
    } shouldBe true
}

private suspend fun assertCreateOrUpdateRole(
    kubernetes: VaultAuthKubernetes,
    givenPath: String,
    expectedReadPath: String
) {
    val given = readJson<KubernetesWriteAuthRolePayload>(givenPath)
    kubernetes.createOrUpdateRole(DEFAULT_ROLE_NAME, given) shouldBe true

    val response = kubernetes.readRole(DEFAULT_ROLE_NAME)
    val expected = readJson<KubernetesReadAuthRoleResponse>(expectedReadPath)
    response shouldBe expected
}
