package me.hechfx.foreground.core.events

enum class ForegroundEventType(val raw: String) {
    COMMIT("#commit"),
    ACCOUNT("#account"),
    IDENTITY("#identity"),
    HANDLE("#handle"),
    TOMBSTONE("#tombstone");

    companion object {
        fun from(raw: String): ForegroundEventType {
            return try {
                entries.first { it.raw == raw }
            } catch (e: NoSuchElementException) {
                throw NoSuchElementException("ForegroundEventType not found for '$raw'")
            }
        }
    }
}
