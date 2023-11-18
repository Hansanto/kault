package io.github.hansanto.kault.system.audit.common

import io.github.hansanto.kault.serializer.EnumSerializer
import kotlinx.serialization.Serializable

/**
 * Enum representing the type of audit device.
 */
@Serializable(AuditTypeSerializer::class)
public enum class AuditType(public val value: String) {
    FILE("file"),
    SOCKET("socket"),
    SYSLOG("syslog")
}

/**
 * Serializer for [AuditType].
 */
public object AuditTypeSerializer : EnumSerializer<AuditType>("auditType", AuditType.entries, { it.value })
