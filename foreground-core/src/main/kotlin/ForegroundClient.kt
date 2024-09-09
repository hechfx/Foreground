package me.hechfx.foreground.core.gateway

import com.upokecenter.cbor.CBORObject
import io.github.oshai.kotlinlogging.KotlinLogging
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.plugins.websocket.*
import io.ktor.client.request.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.serialization.json.Json
import me.hechfx.foreground.core.events.BlueSkyEvent
import me.hechfx.foreground.core.gateway.api.BlueSkyAPIWrapper
import me.hechfx.foreground.core.gateway.api.entity.session.BlueSkySession
import me.hechfx.foreground.core.gateway.api.utils.BlueSkyAPILexicons
import me.hechfx.foreground.core.gateway.utils.builders.ForegroundClientOptionsBuilder
import me.hechfx.foreground.core.utils.BSConstants
import java.time.Duration
import java.time.Instant
import kotlin.math.pow

class ForegroundClient(optionsBuilder: ForegroundClientOptionsBuilder.() -> Unit) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val client = HttpClient(CIO) {
            install(WebSockets) {
                pingInterval = 30_000
            }
            install(ContentNegotiation) {
                json(json = Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    private var _session: ClientWebSocketSession? = null
    val ready = MutableStateFlow(false)
    var blueSkySession: BlueSkySession? = null
    var isShuttingDown = false
    var connectionTries = 1
    var lastEventReceivedAt = Instant.now()
    var lastSequence: Long? = null
    var lastHeaderReceived: CBORObject? = null
    var lastBodyReceived: CBORObject? = null
    var lastEventTime: Instant? = null

    val options = ForegroundClientOptionsBuilder().apply(optionsBuilder).build()
    val session: ClientWebSocketSession
        get() = _session ?: error("Session is not connected!")

    val eventsDispatcher = CoroutineScope(Dispatchers.IO + SupervisorJob())
    val events = MutableSharedFlow<BlueSkyEvent>()
    val api = BlueSkyAPIWrapper(this)
    val gatewayProcessor = BlueSkyGatewayProcessor(this)

    inline fun <reified T : BlueSkyEvent> on(crossinline callback: suspend T.() -> Unit): Job {
        return eventsDispatcher.launch { events.collect { if (it is T) callback(it) } }
    }

    private fun connect() {
        isShuttingDown = false
        ready.value = true
        connectionTries++

        GlobalScope.launch(Dispatchers.IO) {
            logger.info { "Connecting into BlueSky WebSocket..." }

            try {
                val _lastEventTime = lastEventTime

                if (_lastEventTime != null && (System.currentTimeMillis() - _lastEventTime.toEpochMilli()) >= 60_000) {
                    logger.warn { "The last event was sent at $_lastEventTime, and that's too long ago! We aren't going to ask to resume a connection then..." }
                    lastSequence = null
                }

                client.ws(
                    "${BSConstants.BASE_WSS_URL}/${BlueSkyAPILexicons.SUBSCRIBE_REPOS.raw}",
                    {
                        if (lastSequence != null)
                            parameter("cursor", lastSequence)
                    }
                ) {
                    _session = this

                    api.createSession(options.identifier, options.password)

                    // Made by @MrPowerGamerBR
                    // https://github.com/LorittaBot/Loritta/blob/main/yokye/src/main/kotlin/net/perfectdreams/yokye/BlueskyFirehoseClient.kt#L70
                    launch {
                        while (true) {
                            delay(10_000)
                            logger.info { "Checking if Firehose stopped receiving events... Last event received at $lastEventReceivedAt; Event timestamp: $lastEventTime; Last sequence: $lastSequence; Last header: $lastHeaderReceived; Last body: $lastBodyReceived" }
                            val diff = Duration.between(lastEventReceivedAt, Instant.now())

                            if (diff.seconds >= 10) {
                                logger.warn { "Stopped receiving Firehose events! Something may have gone wrong! Restarting..." }
                                this@ws.close()
                                this@ws.cancel()
                                return@launch
                            }

                            delay(1_000)
                        }
                    }

                    // we need to keep the session alive!
                    launch {
                        while (true) {
                            delay(1_800_000) // 30 minutes
                            logger.warn { "The session is expired! Refreshing..." }

                            api.refreshSession()

                            delay(1_000)
                        }
                    }

                    for (frame in incoming) {
                        try {
                            when (frame) {
                                is Frame.Binary -> {
                                    val inputStream = frame.readBytes().inputStream()
                                    val header = CBORObject.Read(inputStream).also {
                                        this@ForegroundClient.lastHeaderReceived = it
                                    }

                                    val op = header["op"].AsInt32()
                                    val t = header["t"]?.AsString()

                                    lastEventReceivedAt = Instant.now()


                                    if (op == -1) {
                                        logger.warn { "Received -1 op, it's an error." }
                                    }

                                    val body = CBORObject.Read(inputStream).also {
                                        this@ForegroundClient.lastBodyReceived = it
                                    }

                                    lastSequence = body["seq"]?.AsInt64Value()

                                    if (body["time"] != null) {
                                        lastEventTime = Instant.parse(body["time"].AsString())
                                    }

                                    if (body["tooBig"]?.AsBoolean() == true) {
                                        logger.warn { "Received a big event" }
                                        continue
                                    }

                                    if (t != null) {
                                        gatewayProcessor.process(t, body)
                                    }
                                }

                                is Frame.Close -> {
                                    logger.warn { "Received shutdown frame!" }
                                    close()
                                    cancel()
                                    return@ws
                                }

                                else -> {
                                    logger.warn { "Received a unhandled frame type! ${frame.frameType}" }
                                }
                            }
                        } catch (e: Exception) {
                            logger.error(e) { "Something went wrong when reading the frames!" }
                        }
                    }
                }
            } catch (e: Exception) {
                logger.error(e) { "Failed to connect to the WebSocket!" }
            }

            val delay = (2.0.pow(connectionTries.toDouble()) * 1_000).toLong()

            logger.warn { "Lost connection with WebSocket! Reconnecting in ${delay}ms..." }

            delay(delay)

            connect()
        }
    }

    fun awaitConnect() {
        connect()

        while (true) {
            if (isShuttingDown) {
                logger.warn { "Shutting down..." }
                break
            }
        }
    }
}

