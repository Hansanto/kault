package io.github.hansanto.kault.identity.entity

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.exception.VaultAPIException
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

class VaultIdentityEntityTest : ShouldSpec({

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
        shouldThrow< VaultAPIException> {
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
        val given = readJson<EntityCreateOrUpdatePayload>("cases/identity/entity/update_by_id/given_create.json")
        val created = identityEntity.createOrUpdateEntity(given)
        created shouldNotBe null

        val updatePayload = readJson<EntityUpdateByIDPayload>("cases/identity/entity/update_by_id/given_update.json")
        val updated = identityEntity.updateEntityByID(created!!.id, updatePayload)
        updated shouldBe true

        val actual = identityEntity.readEntityByID(created.id)
        replaceTemplateString(
            expected = readJson<EntityReadResponse>("cases/identity/entity/update_by_id/expected.json"),
            response = actual,
        ) shouldBe actual
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
        TODO()
    }

    should("create an entity by name with all defined values") {
        TODO()
    }

    should("create an entity by name using builder with default values") {
        TODO()
    }

    should("create an entity by name using builder with all defined values") {
        TODO()
    }

    should("update an entity by name if it exists") {
        TODO()
    }

    should("throw exception when reading a non-existing entity by name") {
        TODO()
    }

    should("return entity info when reading an existing entity by name") {
        TODO()
    }

    should("return true when deleting a non-existing entity by name") {
        TODO()
    }

    should("delete an existing entity by name") {
        TODO()
    }

    should("throw exception when listing the names of the entities with no entities") {
        TODO()
    }

    should("return list of names from existing entities") {
        TODO()
    }

    should("throw exception when merging non-existing source entity") {
        TODO()
    }

    should("throw exception when merging non-existing target entity") {
        TODO()
    }

    should("merge two existing entities") {
        TODO()
    }

})

private suspend fun assertCreateEntity(identityEntity: VaultIdentityEntity, givenPath: String?, expectedReadPath: String) {
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
