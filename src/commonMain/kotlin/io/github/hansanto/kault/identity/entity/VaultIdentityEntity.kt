package io.github.hansanto.kault.identity.entity

import io.github.hansanto.kault.BuilderDsl
import io.github.hansanto.kault.ServiceBuilder
import io.github.hansanto.kault.identity.entity.payload.EntityCreateOrUpdateByIDPayload
import io.github.hansanto.kault.identity.entity.payload.EntityCreateOrUpdateByNamePayload
import io.github.hansanto.kault.identity.entity.payload.EntityMergePayload
import io.github.hansanto.kault.identity.entity.payload.EntityUpdateByIDPayload
import io.github.hansanto.kault.identity.entity.response.EntityCreateResponse
import io.github.hansanto.kault.identity.entity.response.EntityReadResponse
import io.ktor.client.HttpClient
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * @see VaultIdentityEntity.createOrUpdateEntity(payload)
 */
public suspend inline fun VaultIdentityEntity.createOrUpdateEntity(
    builder: BuilderDsl<EntityCreateOrUpdateByIDPayload>
): EntityCreateResponse? {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    val payload = EntityCreateOrUpdateByIDPayload().apply(builder)
    return createOrUpdateEntity(payload)
}

/**
 * @see VaultIdentityEntity.updateEntityByID(id, payload)
 */
public suspend fun VaultIdentityEntity.updateEntityByID(
    id: String,
    builder: BuilderDsl<EntityUpdateByIDPayload>
): Boolean {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    val payload = EntityUpdateByIDPayload().apply(builder)
    return updateEntityByID(id, payload)
}

/**
 * @see VaultIdentityEntity.createOrUpdateEntityByName(name, payload)
 */
public suspend fun VaultIdentityEntity.createOrUpdateEntityByName(
    name: String,
    builder: BuilderDsl<EntityCreateOrUpdateByNamePayload>
): EntityCreateResponse? {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    val payload = EntityCreateOrUpdateByNamePayload().apply(builder)
    return createOrUpdateEntityByName(name, payload)
}

/**
 * @see VaultIdentityEntity.mergeEntities(payload)
 */
public suspend fun VaultIdentityEntity.mergeEntities(
    builder: BuilderDsl<EntityMergePayload.Builder>
): Boolean {
    contract { callsInPlace(builder, InvocationKind.EXACTLY_ONCE) }
    val payload = EntityMergePayload.Builder().apply(builder).build()
    return mergeEntities(payload)
}

public interface VaultIdentityEntity {

    /**
     * This endpoint creates an Entity.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/entity#create-an-entity)
     * @param payload The payload to create the entity.
     * @return `null` if the entity is updated, otherwise returns the response containing the created entity details.
     */
    public suspend fun createOrUpdateEntity(payload: EntityCreateOrUpdateByIDPayload = EntityCreateOrUpdateByIDPayload()): EntityCreateResponse?

    /**
     * This endpoint queries the entity by its identifier.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/entity#read-entity-by-id)
     * @param id Identifier of the entity.
     * @return Returns the response containing the entity details.
     */
    public suspend fun readEntityByID(id: String): EntityReadResponse

    /**
     * This endpoint is used to update an existing entity.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/entity#update-entity-by-id)
     * @param id Identifier of the entity.
     * @param payload The payload to update the entity.
     * @return Returns the response containing the updated entity details.
     */
    public suspend fun updateEntityByID(id: String, payload: EntityUpdateByIDPayload = EntityUpdateByIDPayload()): Boolean

    /**
     * This endpoint deletes an entity and all its associated aliases.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/entity#delete-entity-by-id)
     * @param id Identifier of the entity.
     * @return `true` if the deletion was successful, `false` otherwise.
     */
    public suspend fun deleteEntityByID(id: String): Boolean

    /**
     * This endpoint deletes all entities provided.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/entity#batch-delete-entities)
     * @param entityIds List of entity identifiers to delete.
     * @return `true` if the batch deletion was successful, `false` otherwise.
     */
    public suspend fun batchDeleteEntities(entityIds: Collection<String>): Boolean

    /**
     * This endpoint returns a list of available entities by their identifiers.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/entity#list-entities-by-id)
     * @return List of entity identifiers.
     */
    public suspend fun listEntitiesByID(): List<String>

    /**
     * This endpoint is used to create or update an entity by a given name.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/entity#create-update-entity-by-name)
     * @param name Name of the entity.
     * @param payload The payload to create or update the entity.
     * @return `null` if the entity is updated, otherwise returns the response containing the created entity details.
     */
    public suspend fun createOrUpdateEntityByName(
        name: String,
        payload: EntityCreateOrUpdateByNamePayload = EntityCreateOrUpdateByNamePayload()
    ): EntityCreateResponse?

    /**
     * This endpoint queries the entity by its name.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/entity#read-entity-by-name)
     * @param name Name of the entity.
     * @return Returns the response containing the entity details.
     */
    public suspend fun readEntityByName(name: String): EntityReadResponse

    /**
     * This endpoint deletes an entity and all its associated aliases, given the entity name.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/entity#read-entity-by-name)
     * @param name Name of the entity.
     * @return `true` if the deletion was successful, `false` otherwise.
     */
    public suspend fun deleteEntityByName(name: String): Boolean

    /**
     * This endpoint returns a list of available entities by their names.
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/entity#list-entities-by-name)
     * @return List of entity names.
     */
    public suspend fun listEntitiesByName(): List<String>

    /**
     * This endpoint merges many entities into one entity. Additionally, all groups associated with from_entity_ids are merged with those of to_entity_id. Note that if these entities contain aliases sharing the same mount accessor, the merge will fail unless conflicting_alias_ids_to_keep is present, and entities must be merged one at a time. This is because each entity can only have one alias with each mount accessor - for more information, see the [identity concepts page](https://developer.hashicorp.com/vault/docs/concepts/identity).
     * [Documentation](https://developer.hashicorp.com/vault/api-docs/secret/identity/entity#merge-entities)
     * @param payload The payload to merge the entities.
     * @return `true` if the merge was successful, `false` otherwise.
     */
    public suspend fun mergeEntities(payload: EntityMergePayload): Boolean

}

/**
 * Implementation of [VaultIdentityEntity].
 */
public class VaultIdentityEntityImpl(
    /**
     * HttpClient to make requests.
     */
    private val client: HttpClient,

    /**
     * Base path used to interact with endpoints.
     */
    public val path: String
) : VaultIdentityEntity {

    public companion object {

        public inline operator fun invoke(
            client: HttpClient,
            parentPath: String?,
            builder: BuilderDsl<Builder>
        ): VaultIdentityEntityImpl = Builder().apply(builder).build(client, parentPath)
    }

    /**
     * Companion object to store default values.
     */
    public object Default {

        /**
         * Default API path.
         */
        public const val PATH: String = "entity"
    }

    /**
     * Builder class to simplify the creation of [VaultIdentityEntityImpl].
     */
    public open class Builder : ServiceBuilder<VaultIdentityEntityImpl>() {

        public override var path: String = Default.PATH

        public override fun buildWithCompletePath(client: HttpClient, completePath: String): VaultIdentityEntityImpl =
            VaultIdentityEntityImpl(
                client = client,
                path = completePath
            )
    }

//    override suspend fun createOrUpdateProvider(
//        name: String,
//        payload: OIDCCreateOrUpdateProviderPayload
//    ): Boolean {
//        val response = client.post {
//            url {
//                appendPathSegments(path, "provider", name)
//            }
//            contentType(ContentType.Application.Json)
//            setBody(payload)
//        }
//        return response.status.isSuccess()
//    }

}
