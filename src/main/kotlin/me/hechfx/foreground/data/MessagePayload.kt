package me.hechfx.foreground.data

import kotlinx.serialization.Serializable

@Serializable
data class MessagePayload(
    val uri: String,
    val cid: String
)
