package me.hechfx.foreground.core.gateway.api.entity

import me.hechfx.foreground.core.gateway.api.payloads.*

class Post(
    payload: PostPayload
) {
    val uri = payload.uri
    val cid = payload.cid
    val value = payload.value
}
