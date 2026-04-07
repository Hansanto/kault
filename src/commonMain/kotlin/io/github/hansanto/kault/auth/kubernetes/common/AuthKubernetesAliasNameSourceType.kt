package io.github.hansanto.kault.auth.kubernetes.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

/**
 * Enum representing how identity aliases are generated.
 */
@Serializable(AuthKubernetesAliasNameSourceTypeSerializer::class)
public enum class AuthKubernetesAliasNameSourceType(public val value: String) {
    SERVICE_ACCOUNT_UID("serviceaccount_uid"),
    SERVICE_ACCOUNT_NAME("serviceaccount_name")
}

/**
 * Serializer for [AuthKubernetesAliasNameSourceType].
 */
public object AuthKubernetesAliasNameSourceTypeSerializer : EnumSerializer<AuthKubernetesAliasNameSourceType>(
    "kault.auth.kubernetes.common.AuthKubernetesAliasNameSourceTypeSerializer",
    AuthKubernetesAliasNameSourceType.entries,
    { it.value }
)
