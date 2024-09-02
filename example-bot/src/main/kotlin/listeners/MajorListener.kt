package me.hechfx.bot.listeners

import io.github.oshai.kotlinlogging.*
import me.hechfx.bot.*
import me.hechfx.foreground.core.events.vanilla.commit.CommitEvent
import me.hechfx.foreground.core.events.vanilla.commit.content.ContentType
import me.hechfx.foreground.core.gateway.api.utils.*

class MajorListener(val m: ExampleBot) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    suspend fun listen() = m.client.on<CommitEvent> {
        val trackUsers = listOf(
            "did:plc:tpkrh3jv67mebzcq5xdstq65"
        )

        if (content != null) {
            if (author !in trackUsers)
                return@on

            logger.info { "received a new commit ${content!!::class.simpleName} from $author" }

            when (content) {
                is ContentType.PostContent -> {
                    val content = content as? ContentType.PostContent ?: return@on

                    val post = m.client.api.retrievePostByURI(
                        "at://$author/${ApplicationRepositories.POST.raw}/${
                            properties?.get(0)?.path
                        }"
                    )

                    if (post != null) {
                        if (content.text != null) {
                            m.client.api.createPost {
                                text = content.text!!.reversed()

                                replyTo(post.cid, post.uri)
                            }

                            logger.info { "Successfully sent the reply." }
                        }
                    }
                }

                is ContentType.LikeContent -> {
                    val like = content as ContentType.LikeContent

                    logger.info { "New like to ${like.subject.toURI()}" }
                }

                is ContentType.FollowContent -> {
                    val follow = content as ContentType.FollowContent

                    logger.info { "New follow to ${follow.subjectURI}" }
                }

                else -> {}
            }
        }
    }
}

