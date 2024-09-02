package me.hechfx.foreground.core.gateway.api.utils

enum class ApplicationRepositories(val raw: String) {
    LIKE("app.bsky.feed.like"),
    POST("app.bsky.feed.post"),
    REPOST("app.bsky.feed.repost"),
    PROFILE("app.bsky.actor.profile"),
    DECLARATION("app.bsky.actor.declaration"),
    GENERATOR("app.bsky.feed.generator"),
    CHAT_DECLARATION("chat.bsky.actor.declaration"),
    EMBED_RECORD("app.bsky.embed.record"),
    LIST("app.bsky.graph.list"),
    LIST_ITEM("app.bsky.graph.listitem"),
    LIST_BLOCK("app.bsky.graph.listblock"),
    THREAD_GATE("app.bsky.feed.threadgate"),
    STARTER_PACK("app.bsky.graph.starterpack"),
    POSTGATE("app.bsky.feed.postgate"),
    BLOCK("app.bsky.graph.block"),
    FOLLOW("app.bsky.graph.follow"),
    BLOG_ENTRY("com.whtwnd.blog.entry"),
    POUCH_LINK("com.habitat.pouch.link"),
    LABELER_SERVICE("app.bsky.labeler.service");

    companion object {
        fun from(raw: String): ApplicationRepositories? {
            try {
                return ApplicationRepositories.entries.first { it.raw == raw }
            } catch (e: NoSuchElementException) {
                println("ApplicationRepositories not found for '$raw'")
            }

            return null
        }

        fun fromOrNull(raw: String): ApplicationRepositories? {
            return try {
                from(raw)
            } catch (e: NoSuchElementException) {
                null
            }
        }
    }
}
