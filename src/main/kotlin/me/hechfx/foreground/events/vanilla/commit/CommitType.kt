package me.hechfx.foreground.events.vanilla.commit

enum class CommitType(val raw: String) {
    LIKE("app.bsky.feed.like"),
    POST("app.bsky.feed.post"),
    REPOST("app.bsky.feed.repost"),
    PROFILE("app.bsky.actor.profile"),
    DECLARATION("app.bsky.actor.declaration"),
    GENERATOR("app.bsky.feed.generator"),
    CHAT_DECLARATION("chat.bsky.actor.declaration"),
    LIST("app.bsky.graph.list"),
    LIST_ITEM("app.bsky.graph.listitem"),
    LIST_BLOCK("app.bsky.graph.listblock"),
    THREAD_GATE("app.bsky.feed.threadgate"),
    STARTER_PACK("app.bsky.graph.starterpack"),
    POSTGATE("app.bsky.feed.postgate"),
    BLOCK("app.bsky.graph.block"),
    FOLLOW("app.bsky.graph.follow");

    companion object {
        fun from(raw: String): CommitType {
            return try {
                CommitType.entries.first { it.raw == raw }
            } catch (e: NoSuchElementException) {
                throw NoSuchElementException("CommitType not found for '$raw'")
            }
        }

        fun fromOrNull(raw: String): CommitType? {
            return try {
                from(raw)
            } catch (e: NoSuchElementException) {
                null
            }
        }
    }
}
