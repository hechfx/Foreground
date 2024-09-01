package me.hechfx.foreground.core.gateway.api.utils

enum class APILexicons(val raw: String) {
    SUBSCRIBE_REPOS("com.atproto.sync.subscribeRepos"),
    CREATE_SESSION("com.atproto.server.createSession"),
    RETRIEVE_POST("com.atproto.repo.getRecord"),
    SEND_POST("com.atproto.repo.createRecord");

    companion object {
        fun from(raw: String): APILexicons {
            return try {
                entries.first { it.raw == raw }
            } catch (e: NoSuchElementException) {
                throw NoSuchElementException("APILexicons not found for '$raw'")
            }
        }
    }
}
