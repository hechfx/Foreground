package me.hechfx.bot

import io.github.oshai.kotlinlogging.KotlinLogging
import me.hechfx.bot.utils.config.*
import me.hechfx.foreground.core.events.vanilla.commit.CommitEvent
import me.hechfx.foreground.core.events.vanilla.commit.content.ContentType
import me.hechfx.foreground.core.gateway.ForegroundClient

class ExampleBot(
    val config: ExampleBotConfig
) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    lateinit var client: ForegroundClient

    fun start() {
        client = ForegroundClient {
            identifier = config.identifier
            password = config.password
        }

        client.on<CommitEvent> {
            val trackUsers = listOf(
                "did:plc:tpkrh3jv67mebzcq5xdstq65"
            )

            if (content != null) {
                if (author !in trackUsers)
                    return@on

                logger.info { "received a new commit ${content!!::class.simpleName} from $author" }

                when (content) {
                    is ContentType.LikeContent -> {
                        val like = content as ContentType.LikeContent

                        logger.info { "New like to ${like.subject.prettyURI}" }
                    }

                    is ContentType.FollowContent -> {
                        val follow = content as ContentType.FollowContent

                        logger.info { "New follow to ${follow.subjectURI}" }
                    }

                    else -> {}
                }
            }
        }

        client.awaitConnect()
    }
}
