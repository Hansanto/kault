package io.github.hansanto.kault.auth.kubernetes

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.auth.common.response.LoginResponse
import io.github.hansanto.kault.auth.kubernetes.payload.KubernetesLoginPayload
import io.github.hansanto.kault.auth.kubernetes.payload.KubernetesWriteAuthRolePayload
import io.github.hansanto.kault.auth.kubernetes.response.KubernetesConfigureAuthResponse
import io.github.hansanto.kault.auth.kubernetes.response.KubernetesReadAuthRoleResponse
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.util.DEFAULT_ROLE_NAME
import io.github.hansanto.kault.util.KubernetesUtil
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.enableAuthMethod
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.replaceTemplateString
import io.github.hansanto.kault.util.revokeAllKubernetesData
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe

class VaultAuthKubernetesTest : ShouldSpec({

    lateinit var client: VaultClient
    lateinit var kubernetes: VaultAuthKubernetes

    beforeTest {
        client = createVaultClient()
        kubernetes = client.auth.kubernetes

        enableAuthMethod(client, "kubernetes")

        kubernetes.configure {
            kubernetesHost = KubernetesUtil.host
            kubernetesCaCert = KubernetesUtil.caCert
            tokenReviewerJwt = KubernetesUtil.token
        }

        revokeAllKubernetesData(client)

        shouldThrow<VaultAPIException> { kubernetes.readRole(DEFAULT_ROLE_NAME) }
    }

    afterTest {
        client.close()
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

    should("create a role using builder with default values") {
        assertCreateOrUpdateRoleWithBuilder(
            kubernetes,
            "cases/auth/kubernetes/create/without_options/given.json",
            "cases/auth/kubernetes/create/without_options/expected.json"
        )
    }

    should("create a role using builder with all defined values") {
        assertCreateOrUpdateRoleWithBuilder(
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
        assertLogin(
            kubernetes,
            "cases/auth/kubernetes/login/expected.json"
        ) { role, token -> kubernetes.login(KubernetesLoginPayload(role, token)) }
    }

    should("login using builder with valid token") {
        assertLogin(
            kubernetes,
            "cases/auth/kubernetes/login/expected.json"
        ) { role, token ->
            kubernetes.login {
                this.role = role
                this.jwt = token
            }
        }
    }
})

private suspend inline fun assertLogin(
    kubernetes: VaultAuthKubernetes,
    expectedWritePath: String,
    login: (String, String) -> LoginResponse
) {
    createRole(kubernetes, DEFAULT_ROLE_NAME)

    val response = login(DEFAULT_ROLE_NAME, KubernetesUtil.token)
    val expected = readJson<LoginResponse>(expectedWritePath)
    response shouldBe replaceTemplateString(expected, response)
}

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
    assertCreateOrUpdateRole(kubernetes, givenPath, expectedReadPath) { role, payload ->
        kubernetes.createOrUpdateRole(role, payload)
    }
}

private suspend fun assertCreateOrUpdateRoleWithBuilder(
    kubernetes: VaultAuthKubernetes,
    givenPath: String,
    expectedReadPath: String
) {
    assertCreateOrUpdateRole(kubernetes, givenPath, expectedReadPath) { role, payload ->
        kubernetes.createOrUpdateRole(role) {
            this.boundServiceAccountNames = payload.boundServiceAccountNames
            this.boundServiceAccountNamespaces = payload.boundServiceAccountNamespaces
            this.audience = payload.audience
            this.aliasNameSource = payload.aliasNameSource
            this.tokenTTL = payload.tokenTTL
            this.tokenMaxTTL = payload.tokenMaxTTL
            this.tokenPolicies = payload.tokenPolicies
            this.tokenBoundCidrs = payload.tokenBoundCidrs
            this.tokenExplicitMaxTTL = payload.tokenExplicitMaxTTL
            this.tokenNoDefaultPolicy = payload.tokenNoDefaultPolicy
            this.tokenNumUses = payload.tokenNumUses
            this.tokenPeriod = payload.tokenPeriod
            this.tokenType = payload.tokenType
        }
    }
}

private suspend inline fun assertCreateOrUpdateRole(
    kubernetes: VaultAuthKubernetes,
    givenPath: String,
    expectedReadPath: String,
    createOrUpdate: (String, KubernetesWriteAuthRolePayload) -> Boolean
) {
    val given = readJson<KubernetesWriteAuthRolePayload>(givenPath)
    createOrUpdate(DEFAULT_ROLE_NAME, given) shouldBe true
    kubernetes.readRole(DEFAULT_ROLE_NAME) shouldBe readJson<KubernetesReadAuthRoleResponse>(expectedReadPath)
}
