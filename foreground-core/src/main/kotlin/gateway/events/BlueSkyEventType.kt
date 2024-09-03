package me.hechfx.foreground.core.events

import io.github.oshai.kotlinlogging.KotlinLogging

enum class BlueSkyEventType(val raw: String) {
    COMMIT("#commit"),
    ACCOUNT("#account"),
    IDENTITY("#identity"),
    HANDLE("#handle"),
    TOMBSTONE("#tombstone");

    companion object {
        private val logger = KotlinLogging.logger {}

        fun from(raw: String): BlueSkyEventType? {
            return try {
                entries.first { it.raw == raw }
            } catch (e: NoSuchElementException) {
                logger.warn { "ForegroundEventType not found for '$raw'" }
                null
            }
        }
    }
}
