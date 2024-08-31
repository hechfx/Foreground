package me.hechfx.bot.listeners

import me.hechfx.bot.*
import me.hechfx.foreground.events.vanilla.commit.*
import me.hechfx.foreground.events.vanilla.commit.content.*

class MajorListener(val m: ExampleBot) {
    suspend fun listen() = m.client.on<CommitEvent> {
        if (author != "did:plc:tpkrh3jv67mebzcq5xdstq65")
            return@on

        content.forEach {
            when (it) {
                is ContentType.PostContent -> {
                    if (it.text != null) {
                        if (it.text == "ping") {
                            println("We received ping, then we will send pong!")
                            m.client.post(this,"pong! (via API)")
                        }
                    }
                }

                 else -> {}
            }
        }
    }
}
