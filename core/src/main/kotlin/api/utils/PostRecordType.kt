package me.hechfx.foreground.core.gateway.api.utils

enum class PostRecordType(val raw: String) {
    QUOTE("app.bsky.embed.record"),
    IMAGES("app.bsky.embed.images");

    companion object {
        fun from(raw: String): PostRecordType {
            return try {
                entries.first { it.raw == raw }
            } catch (e: NoSuchElementException) {
                throw NoSuchElementException("ContentType not found for '$raw'")
            }
        }
    }
}
