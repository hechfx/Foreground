package me.hechfx.foreground.core.gateway.api.entity

import kotlinx.serialization.Serializable
import me.hechfx.foreground.core.gateway.api.payloads.*

@Serializable
data class Session(val payload: SessionPayload) {
    val did = payload.did
    val didDoc = payload.didDoc
    val handle = payload.handle
    val email = payload.email
    val emailConfirmed = payload.emailConfirmed
    val emailAuthFactor = payload.emailAuthFactor
    val accessToken = payload.accessToken
    val refreshToken = payload.refreshToken
    val active = payload.active
}
