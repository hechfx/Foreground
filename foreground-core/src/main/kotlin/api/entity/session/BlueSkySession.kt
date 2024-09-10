package me.hechfx.foreground.core.gateway.api.entity.session

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class BlueSkySession(
    val did: String,
    val didDoc: DidDocPayload,
    val handle: String,
    val email: String? = null,
    val emailConfirmed: Boolean? = null,
    val emailAuthFactor: Boolean? = null,
    @SerialName("accessJwt")
    val accessToken: String,
    @SerialName("refreshJwt")
    val refreshToken: String,
    val active: Boolean
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