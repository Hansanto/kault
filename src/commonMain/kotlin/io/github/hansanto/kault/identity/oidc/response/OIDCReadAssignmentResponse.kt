package io.github.hansanto.kault.identity.oidc.response

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
public data class OIDCReadAssignmentResponse(
    /**
     * A list of Vault [entity](https://developer.hashicorp.com/vault/docs/secrets/identity#entities-and-aliases) IDs.
     */
    @SerialName("entity_ids")
    public var entityIds: List<String>,

    /**
     * A list of Vault [group](https://developer.hashicorp.com/vault/docs/secrets/identity#identity-groups) IDs.
     */
    @SerialName("group_ids")
    public var groupIds: List<String>,
)
