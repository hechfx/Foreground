package me.hechfx.foreground.core.gateway.api.entity.post

import kotlinx.serialization.*

@Serializable
data class BlueSkyPost(
    val uri: String,
    val cid: String,
    val value: BlueSkyPostValue
) {
    @Serializable
    data class BlueSkyPostValue(
        @SerialName("\$type")
        val type: String,
        val createdAt: String,
        val langs: List<String>,
        val text: String
    )
}
