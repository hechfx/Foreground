package me.hechfx.foreground.core.data

import kotlinx.serialization.*

@Serializable
data class SessionPayload(
    val did: String,
    val didDoc: DidDocPayload,
    val handle: String,
    val email: String,
    val emailConfirmed: Boolean,
    val emailAuthFactor: Boolean,
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
