package com.github.hansanto.kault.system.audit.payload

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class AuditingEnableDevicePayload(
    /**
     * Specifies a human-friendly description of the audit device.
     */
    @SerialName("description")
    public var description: String? = null,

    /**
     * Specifies if the audit device is local within the cluster only. Local audit devices are not replicated nor (if a secondary) removed by replication.
     */
    @SerialName("local")
    public var local: Boolean? = null,

    /**
     * Specifies configuration options to pass to the audit device itself. For more details, please see the relevant page for an audit device type, under [Audit Devices docs](https://developer.hashicorp.com/vault/docs/audit).
     */
    @SerialName("options")
    public var options: Map<String, String>? = null,

    /**
     * Specifies the type of the audit device. Valid types are file, socket and syslog.
     */
    @SerialName("type")
    public var type: String? = null,
)
