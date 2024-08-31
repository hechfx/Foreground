package me.hechfx.bot

import com.typesafe.config.*
import kotlinx.coroutines.*
import kotlinx.serialization.hocon.*
import me.hechfx.bot.utils.config.*
import java.io.*
import kotlin.system.*

object ExampleBotLauncher {
    @JvmStatic
    fun main(args: Array<String>) {
        val configFile = File("./config.conf")

        if (!configFile.exists()) {
            copyFromJar("/config.conf", "./config.conf")
            exitProcess(1)
        }

        val parsed: ExampleBotConfig = Hocon.decodeFromConfig(
            ConfigFactory.parseFile(configFile)
        )

        val bot = ExampleBot(parsed)

        runBlocking {
            bot.start()
        }
    }

    private fun copyFromJar(inputPath: String, outputPath: String) {
        val inputStream = ExampleBotLauncher::class.java.getResourceAsStream(inputPath)
        val outputStream = FileOutputStream(outputPath)

        inputStream.use { input ->
            outputStream.use { output ->
                input.copyTo(output)
            }
        }
    }
}
