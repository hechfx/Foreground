package me.hechfx.foreground.core.gateway.api.payloads

import kotlinx.serialization.*

@Serializable
data class PostPayload(
    val uri: String,
    val cid: String,
    val value: PostValuePayload
) {
    @Serializable
    data class PostValuePayload(
        @SerialName("\$type")
        val type: String,
        val createdAt: String,
        val langs: List<String>,
        val text: String
    )
}
