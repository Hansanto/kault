package io.github.hansanto.kault.identity.oidc.response

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException

private const val BASE64_TEMPLATE = "eyJmaWVsZDEiOiJ2YWx1ZTEiLCJmaWVsZDIiOjQyLCJmaWVsZDMiOnsic3ViRmllbGQxIjoic3ViVmFsdWUxIiwic3ViRmllbGQyIjp0cnVlfX0="
private const val JSON_TEMPLATE = """{"field1":"value1","field2":42,"field3":{"subField1":"subValue1","subField2":true}}"""

class OIDCReadScopeResponseTest :
    ShouldSpec({

        should("throw when decoding template with wrong base64 flag") {
            val payload = OIDCReadScopeResponse(
                template = BASE64_TEMPLATE,
                description = ""
            )
            shouldThrow<SerializationException> {
                payload.template<CustomTemplate>(base64 = false)
            }
        }

        should("decode template with correct base64 flag") {
            val payload = OIDCReadScopeResponse(
                template = BASE64_TEMPLATE,
                description = ""
            )
            val template = payload.template<CustomTemplate>(base64 = true)
            template shouldBe CustomTemplate(
                field1 = "value1",
                field2 = 42,
                field3 = CustomTemplate.SubField(
                    subField1 = "subValue1",
                    subField2 = true
                )
            )
        }

        should("decode template without base64") {
            val payload = OIDCReadScopeResponse(
                template = JSON_TEMPLATE,
                description = ""
            )
            val template = payload.template<CustomTemplate>(base64 = false)
            template shouldBe CustomTemplate(
                field1 = "value1",
                field2 = 42,
                field3 = CustomTemplate.SubField(
                    subField1 = "subValue1",
                    subField2 = true
                )
            )
        }
    })

@Serializable
private data class CustomTemplate(
    val field1: String,
    val field2: Int,
    val field3: SubField
) {

    @Serializable
    data class SubField(
        val subField1: String,
        val subField2: Boolean
    )
}
