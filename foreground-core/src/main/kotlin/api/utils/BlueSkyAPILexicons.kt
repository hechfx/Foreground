package me.hechfx.foreground.core.gateway.api.utils

enum class BlueSkyAPILexicons(val raw: String) {
    SUBSCRIBE_REPOS("com.atproto.sync.subscribeRepos"),
    CREATE_SESSION("com.atproto.server.createSession"),
    GET_SESSION("com.atproto.server.getSession"),
    REFRESH_SESSION("com.atproto.server.refreshSession"),
    RETRIEVE_POST("com.atproto.repo.getRecord"),
    SEND_POST("com.atproto.repo.createRecord");

    companion object {
        fun from(raw: String): BlueSkyAPILexicons {
            return try {
                entries.first { it.raw == raw }
            } catch (e: NoSuchElementException) {
                throw NoSuchElementException("APILexicons not found for '$raw'")
            }
        }
    }
}
