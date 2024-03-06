package io.github.hansanto.kault.auth.kubernetes.common

import io.github.hansanto.kault.auth.approle.common.TokenType
import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

/**
 * Enum representing how identity aliases are generated.
 */
@Serializable(AliasNameSourceTypeSerializer::class)
public enum class AliasNameSourceType(public val value: String) {
    SERVICE_ACCOUNT_UID("serviceaccount_uid"),
    SERVICE_ACCOUNT_NAME("serviceaccount_name");
}

/**
 * Serializer for [TokenType].
 */
public object AliasNameSourceTypeSerializer : EnumSerializer<AliasNameSourceType>("aliasNameSource", AliasNameSourceType.entries, { it.value })
