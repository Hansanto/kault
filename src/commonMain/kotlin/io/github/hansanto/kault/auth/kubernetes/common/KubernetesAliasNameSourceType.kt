package io.github.hansanto.kault.auth.kubernetes.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

/**
 * Enum representing how identity aliases are generated.
 */
@Serializable(KubernetesAliasNameSourceTypeSerializer::class)
public enum class KubernetesAliasNameSourceType(public val value: String) {
    SERVICE_ACCOUNT_UID("serviceaccount_uid"),
    SERVICE_ACCOUNT_NAME("serviceaccount_name")
}

/**
 * Serializer for [KubernetesAliasNameSourceType].
 */
public object KubernetesAliasNameSourceTypeSerializer : EnumSerializer<KubernetesAliasNameSourceType>(
    KubernetesAliasNameSourceType::class.qualifiedName!!,
    KubernetesAliasNameSourceType.entries,
    { it.value }
)
