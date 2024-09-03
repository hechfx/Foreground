package me.hechfx.foreground.core.gateway.api.utils

import io.github.oshai.kotlinlogging.KotlinLogging

enum class PostRecordType(val raw: String) {
    QUOTE("app.bsky.embed.record"),
    IMAGES("app.bsky.embed.images");

    companion object {
        private val logger = KotlinLogging.logger {}

        fun from(raw: String): PostRecordType? {
            return try {
                entries.first { it.raw == raw }
            } catch (e: NoSuchElementException) {
                logger.warn { "ContentType not found for '$raw'" }
                null
            }
        }
    }
}
