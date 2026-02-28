package io.github.hansanto.kault.identity.entity

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
import io.github.hansanto.kault.identity.entity.payload.EntityCreateOrUpdateByNamePayload
import io.github.hansanto.kault.identity.entity.payload.EntityCreateOrUpdatePayload
import io.github.hansanto.kault.identity.entity.payload.EntityUpdateByIDPayload
import io.github.hansanto.kault.identity.entity.response.EntityCreateResponse
import io.github.hansanto.kault.identity.entity.response.EntityReadResponse
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.replaceTemplateString
import io.github.hansanto.kault.util.revokeEntity
import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain

class VaultIdentityEntityTest :
    ShouldSpec({

        lateinit var client: VaultClient
        lateinit var identityEntity: VaultIdentityEntity

        beforeTest {
            client = createVaultClient()
            identityEntity = client.identity.entity
        }

        afterTest {
            revokeEntity(client)
            client.close()
        }

        should("use default path if not set in builder") {
            VaultIdentityEntityImpl.Default.PATH shouldBe "entity"

            val built = VaultIdentityEntityImpl.Companion(client.client, null) {
            }

            built.path shouldBe VaultIdentityEntityImpl.Default.PATH
        }

        should("use custom path if set in builder") {
            val builderPath = randomString()
            val parentPath = randomString()

            val built = VaultIdentityEntityImpl.Companion(client.client, parentPath) {
                path = builderPath
            }

            built.path shouldBe "$parentPath/$builderPath"
        }

        should("create an entity with default values") {
            assertCreateEntity(
                identityEntity,
                null,
                "cases/identity/entity/create/without_options/expected.json"
            )
        }

        should("create an entity with all defined values") {
            assertCreateEntity(
                identityEntity,
                "cases/identity/entity/create/with_options/given.json",
                "cases/identity/entity/create/with_options/expected.json"
            )
        }

        should("create an entity using builder with default values") {
            assertCreateEntityWithBuilder(
                identityEntity,
                null,
                "cases/identity/entity/create/without_options/expected.json"
            )
        }

        should("create an entity using builder with all defined values") {
            assertCreateEntityWithBuilder(
                identityEntity,
                "cases/identity/entity/create/with_options/given.json",
                "cases/identity/entity/create/with_options/expected.json"
            )
        }

        should("update an entity if it exists") {
            val given = readJson<EntityCreateOrUpdatePayload>("cases/identity/entity/update/given_create.json")
            val created = identityEntity.createOrUpdateEntity(given)
            created shouldNotBe null

            val updatePayload = readJson<EntityCreateOrUpdatePayload>("cases/identity/entity/update/given_update.json")
            updatePayload.id = created!!.id
            val updated = identityEntity.createOrUpdateEntity(updatePayload)
            updated shouldBe null

            val actual = identityEntity.readEntityByID(created.id)
            replaceTemplateString(
                expected = readJson<EntityReadResponse>("cases/identity/entity/update/expected.json"),
                response = actual,
            ) shouldBe actual
        }

        should("throw exception when reading a non-existing entity by id") {
            shouldThrow<VaultAPIException> {
                identityEntity.readEntityByID("non-existing-id")
            }
        }

        should("read existing entity by id") {
            val response = identityEntity.createOrUpdateEntity()
            response shouldNotBe null

            val actual = identityEntity.readEntityByID(response!!.id)
            actual.id shouldBe response.id
            actual.name shouldBe response.name
        }

        should("throw exception when updating a non-existing entity by id") {
            shouldThrow<VaultAPIException> {
                identityEntity.updateEntityByID("non-existing-id")
            }
        }

        should("update an existing entity by id") {
            assertUpdateEntityByID(
                identityEntity,
                "cases/identity/entity/update_by_id/given_create.json",
                "cases/identity/entity/update_by_id/given_update.json",
                "cases/identity/entity/update_by_id/expected.json"
            )
        }

        should("update an existing entity by id with builder") {
            assertUpdateEntityByIDWithBuilder(
                identityEntity,
                "cases/identity/entity/update_by_id/given_create.json",
                "cases/identity/entity/update_by_id/given_update.json",
                "cases/identity/entity/update_by_id/expected.json"
            )
        }

        should("return true when deleting a non-existing entity by id") {
            val id = "non-existing-id"
            shouldThrow<VaultAPIException> { identityEntity.readEntityByID(id) }
            identityEntity.deleteEntityByID(id) shouldBe true
            shouldThrow<VaultAPIException> { identityEntity.readEntityByID(id) }
        }

        should("delete existing entity by id") {
            val response = identityEntity.createOrUpdateEntity()
            val id = response!!.id
            shouldNotThrow<VaultAPIException> { identityEntity.readEntityByID(id) }
            identityEntity.deleteEntityByID(id) shouldBe true
            shouldThrow<VaultAPIException> { identityEntity.readEntityByID(id) }
        }

        should("return true when deleting a non-existing entities by batch") {
            val ids = listOf("non-existing-id-1", "non-existing-id-2", "non-existing-id-3")
            ids.forEach { shouldThrow<VaultAPIException> { identityEntity.readEntityByID(it) } }
            identityEntity.batchDeleteEntities(ids) shouldBe true
            ids.forEach { shouldThrow<VaultAPIException> { identityEntity.readEntityByID(it) } }
        }

        should("return true when deleting partial existing entities by batch") {
            val existingIds = List(3) {
                val response = identityEntity.createOrUpdateEntity()
                response!!.id
            }
            val nonExistingIds = existingIds.map { "fake-$it" }
            val allIds = existingIds + nonExistingIds

            identityEntity.batchDeleteEntities(allIds) shouldBe true
            allIds.forEach { shouldThrow<VaultAPIException> { identityEntity.readEntityByID(it) } }
        }

        should("delete existing entities by batch") {
            val ids = List(3) {
                val response = identityEntity.createOrUpdateEntity()
                response!!.id
            }

            identityEntity.batchDeleteEntities(ids) shouldBe true
            ids.forEach { shouldThrow<VaultAPIException> { identityEntity.readEntityByID(it) } }
        }

        should("throw exception when listing the ids of the entities with no entities") {
            shouldThrow<VaultAPIException> {
                identityEntity.listEntitiesByID()
            }
        }

        should("return list of ids from existing entities") {
            val createdIds = List(3) {
                identityEntity.createOrUpdateEntity()!!
            }

            val listEntitiesByID = identityEntity.listEntitiesByID()
            listEntitiesByID.keys shouldContainExactlyInAnyOrder createdIds.map { it.id }
            listEntitiesByID.keyInfo.values.map { it.name } shouldContainExactlyInAnyOrder createdIds.map { it.name }
        }

        should("create an entity by name with default values") {
            assertCreateEntityByName(
                "create_entity_by_name",
                identityEntity,
                null,
                "cases/identity/entity/create_by_name/without_options/expected.json"
            )
        }

        should("create an entity by name with all defined values") {
            assertCreateEntityByName(
                "create_entity_by_name",
                identityEntity,
                "cases/identity/entity/create_by_name/with_options/given.json",
                "cases/identity/entity/create_by_name/with_options/expected.json"
            )
        }

        should("create an entity by name using builder with default values") {
            assertCreateEntityByNameWithBuilder(
                "create_entity_by_name",
                identityEntity,
                null,
                "cases/identity/entity/create_by_name/without_options/expected.json"
            )
        }

        should("create an entity by name using builder with all defined values") {
            assertCreateEntityByNameWithBuilder(
                "create_entity_by_name",
                identityEntity,
                "cases/identity/entity/create_by_name/with_options/given.json",
                "cases/identity/entity/create_by_name/with_options/expected.json"
            )
        }

        should("update an entity by name if it exists") {
            val name = "update_entity_by_name"
            val given =
                readJson<EntityCreateOrUpdateByNamePayload>("cases/identity/entity/update_by_name/given_create.json")
            val created = identityEntity.createOrUpdateEntityByName(name, given)
            created shouldNotBe null

            val updatePayload =
                readJson<EntityCreateOrUpdateByNamePayload>("cases/identity/entity/update_by_name/given_update.json")
            val updated = identityEntity.createOrUpdateEntityByName(name, updatePayload)
            updated shouldBe null

            val actual = identityEntity.readEntityByName(name)
            replaceTemplateString(
                expected = readJson<EntityReadResponse>("cases/identity/entity/update_by_name/expected.json"),
                response = actual,
            ) shouldBe actual
        }

        should("throw exception when reading a non-existing entity by name") {
            shouldThrow<VaultAPIException> {
                identityEntity.readEntityByName("non-existing-name")
            }
        }

        should("read existing entity by name") {
            val name = "read_entity_by_name"
            val response = identityEntity.createOrUpdateEntityByName(name)
            response shouldNotBe null

            val actual = identityEntity.readEntityByName(name)
            actual.id shouldBe response!!.id
            actual.name shouldBe response.name
        }

        should("return true when deleting a non-existing entity by name") {
            val name = "delete_non_existing_entity_by_name"
            shouldThrow<VaultAPIException> { identityEntity.readEntityByName(name) }
            identityEntity.deleteEntityByName(name) shouldBe true
            shouldThrow<VaultAPIException> { identityEntity.readEntityByName(name) }
        }

        should("delete existing entity by name") {
            val name = "delete_entity_by_name"
            val response = identityEntity.createOrUpdateEntityByName(name)
            val id = response!!.id
            shouldNotThrow<VaultAPIException> { identityEntity.readEntityByID(id) }
            identityEntity.deleteEntityByName(name) shouldBe true
            shouldThrow<VaultAPIException> { identityEntity.readEntityByID(id) }
        }

        should("throw exception when listing the names of the entities with no entities") {
            shouldThrow<VaultAPIException> {
                identityEntity.listEntitiesByName()
            }
        }

        should("return list of names from existing entities") {
            val expectedListName = List(3) { "merge_non_existing_source_entity_$it" }
            expectedListName.forEach { identityEntity.createOrUpdateEntityByName(it) }

            val listEntitiesByName = identityEntity.listEntitiesByName()
            listEntitiesByName shouldContainExactlyInAnyOrder expectedListName
        }

        should("throw exception when merging non-existing source entity") {
            val targetEntity = identityEntity.createOrUpdateEntity()!!

            val ex = shouldThrow<VaultAPIException> {
                identityEntity.mergeEntities {
                    fromEntityIds = listOf("non-existing-id")
                    toEntityId = targetEntity.id
                }
            }
            ex.message shouldContain "entity id to merge from is invalid"
        }

        should("throw exception when merging non-existing target entity") {
            val sourceEntity = identityEntity.createOrUpdateEntity()!!

            val ex = shouldThrow<VaultAPIException> {
                identityEntity.mergeEntities {
                    fromEntityIds = listOf(sourceEntity.id)
                    toEntityId = "non-existing-id"
                }
            }
            ex.message shouldContain "entity id to merge to is invalid"
        }

        should("merge two existing entities") {
            // TODO: Add checks after implementation of services:
            //  - To check if the groups are merged correctly in the target entity: need GROUP API https://developer.hashicorp.com/vault/api-docs/secret/identity/group#update-group-by-id
            //  - To check 'conflicting_alias_ids_to_keep': need ENTITY ALIAS API https://developer.hashicorp.com/vault/api-docs/secret/identity/entity-alias#create-or-update-entity-alias
            //  - To check 'force' property: need MFA API (TOTP) https://developer.hashicorp.com/vault/api-docs/secret/identity/mfa
            val sourceEntity1 = identityEntity.createOrUpdateEntity {
                name = "merge_source_entity_1"
            }!!
            val sourceEntity2 = identityEntity.createOrUpdateEntity {
                name = "merge_source_entity_2"
            }!!
            val targetEntity = identityEntity.createOrUpdateEntity()!!

            identityEntity.mergeEntities {
                fromEntityIds = listOf(sourceEntity1.id, sourceEntity2.id)
                toEntityId = targetEntity.id
            }

            shouldThrow<VaultAPIException> { identityEntity.readEntityByID(sourceEntity1.id) }
            shouldThrow<VaultAPIException> { identityEntity.readEntityByID(sourceEntity2.id) }

            val actualTarget = identityEntity.readEntityByID(targetEntity.id)
            actualTarget.id shouldBe targetEntity.id
            actualTarget.mergedEntityIds shouldContainExactlyInAnyOrder listOf(sourceEntity1.id, sourceEntity2.id)
        }
    })

private suspend fun assertCreateEntity(
    identityEntity: VaultIdentityEntity,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateEntity(
        identityEntity,
        givenPath,
        expectedReadPath
    ) { payload ->
        identityEntity.createOrUpdateEntity(payload)
    }
}

private suspend fun assertCreateEntityWithBuilder(
    identityEntity: VaultIdentityEntity,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateEntity(
        identityEntity,
        givenPath,
        expectedReadPath
    ) { payload ->
        identityEntity.createOrUpdateEntity {
            name = payload.name
            id = payload.id
            metadata = payload.metadata
            policies = payload.policies
            disabled = payload.disabled
        }
    }
}

private suspend inline fun assertCreateEntity(
    identityEntity: VaultIdentityEntity,
    givenPath: String?,
    expectedReadPath: String,
    createOrUpdate: (EntityCreateOrUpdatePayload) -> EntityCreateResponse?
) {
    val given =
        givenPath?.let { readJson<EntityCreateOrUpdatePayload>(it) } ?: EntityCreateOrUpdatePayload()
    val result = createOrUpdate(given)

    val id = result?.id
    id shouldNotBe null

    val actual = identityEntity.readEntityByID(id!!)

    replaceTemplateString(
        expected = readJson<EntityReadResponse>(expectedReadPath),
        response = actual,
    ) shouldBe actual
}

private suspend fun assertCreateEntityByName(
    name: String,
    identityEntity: VaultIdentityEntity,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateEntityByName(
        identityEntity,
        givenPath,
        expectedReadPath
    ) { payload ->
        identityEntity.createOrUpdateEntityByName(name, payload)
    }
}

private suspend fun assertCreateEntityByNameWithBuilder(
    name: String,
    identityEntity: VaultIdentityEntity,
    givenPath: String?,
    expectedReadPath: String
) {
    assertCreateEntityByName(
        identityEntity,
        givenPath,
        expectedReadPath
    ) { payload ->
        identityEntity.createOrUpdateEntityByName(name) {
            metadata = payload.metadata
            policies = payload.policies
            disabled = payload.disabled
        }
    }
}

private suspend inline fun assertCreateEntityByName(
    identityEntity: VaultIdentityEntity,
    givenPath: String?,
    expectedReadPath: String,
    createOrUpdate: (EntityCreateOrUpdateByNamePayload) -> EntityCreateResponse?
) {
    val given =
        givenPath?.let { readJson<EntityCreateOrUpdateByNamePayload>(it) } ?: EntityCreateOrUpdateByNamePayload()
    val result = createOrUpdate(given)

    val id = result?.id
    id shouldNotBe null

    val actual = identityEntity.readEntityByID(id!!)

    replaceTemplateString(
        expected = readJson<EntityReadResponse>(expectedReadPath),
        response = actual,
    ) shouldBe actual
}

private suspend fun assertUpdateEntityByID(
    identityEntity: VaultIdentityEntity,
    givenCreatePath: String,
    givenUpdatePath: String,
    expectedReadPath: String,
) {
    assertUpdateEntityByID(
        identityEntity,
        givenCreatePath,
        givenUpdatePath,
        expectedReadPath
    ) { id, payload ->
        identityEntity.updateEntityByID(id, payload)
    }
}

private suspend fun assertUpdateEntityByIDWithBuilder(
    identityEntity: VaultIdentityEntity,
    givenCreatePath: String,
    givenUpdatePath: String,
    expectedReadPath: String,
) {
    assertUpdateEntityByID(
        identityEntity,
        givenCreatePath,
        givenUpdatePath,
        expectedReadPath
    ) { id, payload ->
        identityEntity.updateEntityByID(id) {
            name = payload.name
            metadata = payload.metadata
            policies = payload.policies
            disabled = payload.disabled
        }
    }
}

private suspend inline fun assertUpdateEntityByID(
    identityEntity: VaultIdentityEntity,
    givenCreatePath: String,
    givenUpdatePath: String,
    expectedReadPath: String,
    updateEntityByID: (String, EntityUpdateByIDPayload) -> Boolean
) {
    val given = readJson<EntityCreateOrUpdatePayload>(givenCreatePath)
    val created = identityEntity.createOrUpdateEntity(given)
    created shouldNotBe null

    val updatePayload =
        readJson<EntityUpdateByIDPayload>(givenUpdatePath)
    val updated = updateEntityByID(created!!.id, updatePayload)
    updated shouldBe true

    val actual = identityEntity.readEntityByID(created!!.id)
    replaceTemplateString(
        expected = readJson<EntityReadResponse>(expectedReadPath),
        response = actual,
    ) shouldBe actual
}
