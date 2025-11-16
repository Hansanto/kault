package io.github.hansanto.kault.identity.oidc.payload

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.serialization.Serializable

class OIDCCreateOrUpdateScopePayloadTest :
    ShouldSpec({

        should("stringify json template without base64 with builder") {
            val payload = OIDCCreateOrUpdateScopePayload.Builder().apply {
                template(
                    CustomTemplate(
                        field1 = "value1",
                        field2 = 42,
                        field3 = CustomTemplate.SubField(
                            subField1 = "subValue1",
                            subField2 = true
                        )
                    )
                )
            }.build()
            payload.template shouldBe """{"field1":"value1","field2":42,"field3":{"subField1":"subValue1","subField2":true}}"""
        }

        should("stringify json template with base64 with builder") {
            val payload = OIDCCreateOrUpdateScopePayload.Builder().apply {
                template(
                    CustomTemplate(
                        field1 = "value1",
                        field2 = 42,
                        field3 = CustomTemplate.SubField(
                            subField1 = "subValue1",
                            subField2 = true
                        )
                    ),
                    base64 = true
                )
            }.build()
            payload.template shouldBe "eyJmaWVsZDEiOiJ2YWx1ZTEiLCJmaWVsZDIiOjQyLCJmaWVsZDMiOnsic3ViRmllbGQxIjoic3ViVmFsdWUxIiwic3ViRmllbGQyIjp0cnVlfX0="
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
