package me.hechfx.foreground.core.gateway

import com.upokecenter.cbor.CBORObject
import io.github.oshai.kotlinlogging.*
import io.ktor.client.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import me.hechfx.foreground.core.events.*
import me.hechfx.foreground.core.gateway.api.*
import me.hechfx.foreground.core.gateway.api.entity.*
import me.hechfx.foreground.core.gateway.api.utils.*
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
    var lastSequence: Long? = null // https://github.com/LorittaBot/Loritta/commit/1fd6ffb20b32d81311a9ecc704bec548b5f6147b

    val client = HttpClient(CIO) {
        install(WebSockets) {
            pingInterval = 30_000
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

    suspend inline fun <reified T : ForegroundEvent> on(crossinline callback: suspend T.() -> (Unit)): Job {
        return GlobalScope.launch { events.collect { if (it is T) callback(it) } }
    }

    suspend fun connect() {
        isShuttingDown = false
        connectionTries++

        api.createSession(identifier, password)

        try {
            client.ws(
                "$BASE_URL/${APILexicons.SUBSCRIBE_REPOS.raw}",
                {
                    if (lastSequence != null)
                        parameter("cursor", lastSequence)
                }
            ) {
                _session = this

                logger.info { "Logged successfully into BlueSky's API!" }
                logger.info { "Estabilished connection to BlueSky's WebSocket!" }

                while (true) {
                    val frame = incoming.receive() as? Frame.Binary

                    if (frame != null) {
                        val frameInputStream = frame.readBytes().inputStream()
                        val header = CBORObject.Read(frameInputStream)
                        val op = header["op"].AsInt32()
                        val t = header["t"]?.AsString()

                        if (op == -1) {
                            logger.warn { "Received -1 op, it's an error. Let's re-do the connection." }
                            cancel()
                            return@ws
                        }

                        val body = CBORObject.Read(frameInputStream)
                        lastSequence = body["seq"]?.AsInt64Value()

                        // seriously? if it's too big just continue the loop
                        // for some reason if the event is too big the loop just gets stuck
                        // and... for some reason this solves the problem
                        if (body["tooBig"]?.AsBoolean() == true)
                            continue

                        if (t != null) {
                            gatewayProcessor.process(t, body)
                        }
                    } else {
                        logger.warn { "Received a non-binary frame!" }
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn { "Failed to connect to BlueSky's WebSocket! ${e.message}" }
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
