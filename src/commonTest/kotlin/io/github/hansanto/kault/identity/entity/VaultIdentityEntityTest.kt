package io.github.hansanto.kault.identity.entity

import io.github.hansanto.kault.VaultClient
import io.github.hansanto.kault.identity.entity.payload.EntityCreateOrUpdatePayload
import io.github.hansanto.kault.identity.entity.response.EntityCreateResponse
import io.github.hansanto.kault.identity.entity.response.EntityReadResponse
import io.github.hansanto.kault.util.createVaultClient
import io.github.hansanto.kault.util.randomString
import io.github.hansanto.kault.util.readJson
import io.github.hansanto.kault.util.replaceTemplateString
import io.github.hansanto.kault.util.revokeEntity
import io.kotest.core.spec.style.ShouldSpec
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
        TODO()
    }

    should("throw exception when reading a non-existing entity by id") {
        TODO()
    }

    should("return entity info when reading an existing entity by id") {
        TODO()
    }

    should("throw exception when updating a non-existing entity by id") {
        TODO()
    }

    should("update an existing entity by id") {
        TODO()
    }

    should("return true when deleting a non-existing entity by id") {
        TODO()
    }

    should("delete an existing entity by id") {
        TODO()
    }

    should("return true when deleting a non-existing entities by batch") {
        TODO()
    }

    should("return true when deleting partial existing entities by batch") {
        TODO()
    }

    should("delete existing entities by batch") {
        TODO()
    }

    should("throw exception when listing the ids of the entities with no entities") {
        TODO()
    }

    should("return list of ids from existing entities") {
        TODO()
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

    println("Create/Update Result: $result")
    val id = result?.id
    id shouldNotBe null

    val actual = identityEntity.readEntityByID(id!!)

    replaceTemplateString(
        expected = readJson<EntityReadResponse>(expectedReadPath),
        response = actual,
    ).apply {
        println(this)
    } shouldBe actual
}
