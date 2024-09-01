package me.hechfx.foreground.core.gateway

import com.upokecenter.cbor.CBORObject
import io.github.oshai.kotlinlogging.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import me.hechfx.foreground.core.events.*
import me.hechfx.foreground.core.gateway.api.*
import me.hechfx.foreground.core.gateway.api.entity.*
import me.hechfx.foreground.core.gateway.api.utils.*
import java.util.concurrent.*
import kotlin.math.*

class ForegroundWebSocketClient(
    val identifier: String,
    val password: String
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        const val BASE_URL = "wss://bsky.network/xrpc"
    }

    var bsSession: Session? = null
    private var _session: ClientWebSocketSession? = null

    var isShuttingDown = false
    var connectionTries = 0
    var lastResponseTime = 0L

    val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 25_000
        }
        install(ContentNegotiation) {
            json(json = Json {
                ignoreUnknownKeys = true
            })
        }
    }

    val session: ClientWebSocketSession
        get() = _session ?: error("Session is not connected!")

    val events = MutableSharedFlow<ForegroundEvent>()
    val api = ForegroundAPIHandler(this)
    val gatewayProcessor = ForegroundGatewayProcessor(this)
    val executors = Executors.newScheduledThreadPool(1)

    suspend inline fun <reified T : ForegroundEvent> on(crossinline callback: suspend T.() -> (Unit)): Job {
        return GlobalScope.launch { events.collect { if (it is T) callback(it) } }
    }

    suspend fun connect() {
        isShuttingDown = false
        connectionTries++

        api.createSession(identifier, password)

        try {
            client.webSocket("$BASE_URL/${APILexicons.SUBSCRIBE_REPOS.raw}") {
                _session = this

                logger.info { "Logged successfully into BlueSky's API!" }
                logger.info { "Estabilished connection to BlueSky's WebSocket!" }

                // This is a hacky solution because firehose is literally STOPPING sending frames.
                // Need to figure out why this is happening.
                // So, for now, this should be enough.
                executors.scheduleWithFixedDelay(
                    Runnable {
                        GlobalScope.launch {
                            if (System.currentTimeMillis() - lastResponseTime > 5000 && lastResponseTime != 0L) {
                                logger.warn { "Connection is idle for more than 5 seconds! Reconnecting..." }
                                bsSession = null
                                lastResponseTime = 0L
                                shutdownSession()
                                connect()
                                cancel("Cancelling coroutine to avoid stacking...")
                            }
                        }
                    },
                    0L,
                    1L,
                    TimeUnit.SECONDS
                )

                while (true) {
                    val frame = incoming.receive() as? Frame.Binary

                    if (frame != null) {
                        lastResponseTime = System.currentTimeMillis()

                        val frameInputStream = frame.readBytes().inputStream()
                        val header = CBORObject.Read(frameInputStream)
                        val op = header.get("op").AsInt32()
                        val t = header.get("t").AsString()

                        if (op == -1) {
                            val error = header.get("header")?.AsString()
                            val message = header.get("message")?.AsString()

                            logger.warn("$error; $message") { "Received -1 op, it's an error. Let's re-do the connection." }
                            bsSession = null
                            return@webSocket
                        }

                        gatewayProcessor.process(t, frameInputStream)
                    } else {
                        logger.warn { "Received a non-binary frame!" }
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to connect to BlueSky's WebSocket!" }
            bsSession = null
        }

        val delay = (2.0.pow(connectionTries.toDouble()) * 1_000).toLong()

        logger.warn { "Lost connection with WebSocket! Reconnecting after ${delay}ms..." }
        shutdownSession()
        delay(delay)
        connect()
    }

    suspend fun shutdownSession() {
        isShuttingDown = true

        if (session.isActive) {
            try {
                session.close()
            } catch (e: Exception) {
                logger.warn(e) { "Failed to shutdown the session!" }
            }
        }
    }
}
