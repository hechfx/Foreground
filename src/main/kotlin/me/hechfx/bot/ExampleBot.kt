package me.hechfx.bot

import me.hechfx.bot.listeners.*
import me.hechfx.bot.utils.config.*
import me.hechfx.foreground.*

class ExampleBot(
    val config: ExampleBotConfig
) {
    lateinit var client: ForegroundWebSocketClient

    suspend fun start() {
        client = ForegroundWebSocketClient(
            config.identifier,
            config.password
        )

        MajorListener(this).listen()

        client.connect()
    }
}
