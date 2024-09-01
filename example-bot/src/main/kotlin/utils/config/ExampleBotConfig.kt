package me.hechfx.bot.utils.config

import kotlinx.serialization.Serializable

@Serializable
data class ExampleBotConfig(
    val identifier: String,
    val password: String
)
