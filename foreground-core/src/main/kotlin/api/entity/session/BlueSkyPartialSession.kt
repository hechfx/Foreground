package me.hechfx.foreground.core.gateway.api.entity.session

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlueSkyPartialSession(
    val handle: String,
    val did: String,
    val email: String,
    val emailConfirmed: Boolean,
    val emailAuthFactor: Boolean,
    val didDoc: DidDocPayload,
    val active: Boolean,
    val status: String
) {
    @Serializable
    data class DidDocPayload(
        @SerialName("@context")
        val context: List<String>,
        val id: String,
        val alsoKnownAs: List<String>,
        val verificationMethod: List<VerificationMethodPayload>,
        val service: List<ServicePayload>
    ) {
        @Serializable
        data class VerificationMethodPayload(
            val id: String,
            val type: String,
            val controller: String,
            val publicKeyMultibase: String
        )

        @Serializable
        data class ServicePayload(
            val id: String,
            val type: String,
            val serviceEndpoint: String
        )
    }
}