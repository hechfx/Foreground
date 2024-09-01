package me.hechfx.bot.listeners

import me.hechfx.bot.*
import me.hechfx.foreground.core.events.vanilla.commit.CommitEvent
import me.hechfx.foreground.core.events.vanilla.commit.content.ContentType
import me.hechfx.foreground.core.gateway.api.utils.*

class MajorListener(val m: ExampleBot) {
    suspend fun listen() = m.client.on<CommitEvent> {
        val trackUsers = listOf(
            "did:plc:tpkrh3jv67mebzcq5xdstq65",
            "did:plc:sjxjio2cekseh2j5blxvidxk"
        )

        if (content != null) {
            if (author !in trackUsers)
                return@on

            println("received a new commit ${content!!::class.simpleName} from $author")

            when (content) {
                is ContentType.PostContent -> {
                    val content = content as? ContentType.PostContent ?: return@on

                    val post = m.client.api.retrievePostByURI(
                        "at://$author/${ApplicationRepositories.POST.raw}/${
                            properties?.get(0)?.path
                        }"
                    )

                    if (post != null) {
                        println("the post is not null, nice.")
                        if (content.text != null) {
                            println("the text is not null too, nice.")

                            m.client.api.createPost {
                                text = content.text!!.reversed()

                                replyTo(post.cid, post.uri)
                            }
                        }
                    }
                }

                is ContentType.LikeContent -> {
                    val like = content as ContentType.LikeContent

                    println("New like to ${like.subject.toURI()}")
                }

                is ContentType.FollowContent -> {
                    val follow = content as ContentType.FollowContent

                    println("New follow to ${follow.subjectURI}")
                }

                else -> {}
            }
        }
    }
}

