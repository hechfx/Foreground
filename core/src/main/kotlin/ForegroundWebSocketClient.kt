package me.hechfx.foreground.core.gateway

import com.upokecenter.cbor.CBORObject
import io.github.oshai.kotlinlogging.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.websocket.*
import kotlinx.coroutines.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.websocket.*
import kotlinx.coroutines.flow.*
import kotlinx.serialization.json.*
import me.hechfx.foreground.core.data.*
import me.hechfx.foreground.core.events.*
import me.hechfx.foreground.core.events.vanilla.commit.*
import me.hechfx.foreground.core.gateway.*
import me.hechfx.foreground.core.utils.*
import java.time.*
import kotlin.math.*

class ForegroundWebSocketClient(
    val identifier: String,
    val password: String
) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val client = HttpClient(CIO) {
            install(WebSockets)
            install(ContentNegotiation) {
                json(json = Json {
                    ignoreUnknownKeys = true
                })
            }
        }
        const val BASE_URL = "wss://bsky.network/xrpc"
    }

    private var sessionPayload: SessionPayload? = null
    private var _session: ClientWebSocketSession? = null
    private val gatewayProcessor = ForegroundGatewayProcessor(this)

    var isShuttingDown = false
    var connectionTries = 0

    val session: ClientWebSocketSession
        get() = _session ?: error("Session is not connected!")

    val eventsDispatcher = CoroutineScope(Dispatchers.Default + SupervisorJob())
    val events = MutableSharedFlow<ForegroundEvent>()

    suspend inline fun <reified T : ForegroundEvent> on(crossinline callback: suspend T.() -> (Unit)): Job {
        return eventsDispatcher.launch {
            events.mapNotNull { it as? T }.collect { callback(it) }
        }
    }

    suspend fun connect() {
        isShuttingDown = false
        connectionTries++

        try {
            sessionPayload = login()

            if (sessionPayload != null) {
                logger.info { "Logged successfully!" }
            }

            client.ws("$BASE_URL/com.atproto.sync.subscribeRepos") {
                _session = this

                logger.info { "Estabilishing connection into BlueSky's WebSocket..." }

                while (true) {
                    for (frame in incoming) {
                        try {
                            when (frame) {
                                is Frame.Binary -> {
                                    data class BSHeaderPayload(
                                        val op: Int,
                                        val t: String
                                    )

                                    val frameInputStream = frame.readBytes().inputStream()
                                    val rawHeader = CBORObject.Read(frameInputStream)
                                    val header = BSHeaderPayload(
                                        rawHeader.get("op").AsInt32(),
                                        rawHeader.get("t").AsString()
                                    )

                                    gatewayProcessor.process(header.t, frameInputStream)
                                }

                                is Frame.Close -> {
                                    logger.warn { "WebSocket connection closed!" }
                                    shutdownSession()
                                    return@ws
                                }

                                else -> logger.warn { "Unknown frame type!" }
                            }
                        } catch (e: Exception) {
                            logger.warn(e) { "Something went wrong while reading frames!" }
                        }
                    }
                }
            }
        } catch (e: Exception) {
            logger.warn(e) { "Failed to connect to BlueSky's WebSocket!" }
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

    suspend fun getPost(uri: String): MessagePayload? {
        val begin = uri.substring("at://".length)
        val author = begin.split("/")[0]
        val collection = begin.split("/")[1]
        val rKey = begin.split("/")[2]

        val builtUrl = buildString {
            append("${BSConstants.BASE_API_URL}/com.atproto.repo.getRecord")
            append("?repo=$author&")
            append("collection=$collection&")
            append("rkey=$rKey")
        }

        return client.get {
            url(builtUrl)
        }.body()
    }

    suspend fun post(message: CommitEvent, content: String) {
        if (sessionPayload == null)
            throw RuntimeException("You can't post something without logging!")

        val now = Instant.now().toString().replace("+00:00", "Z")

        val uri = "at://${message.author}/${CommitType.POST.raw}/${message.properties?.get(0)?.path}"
        val parent = getPost(uri) ?: throw RuntimeException("Couldn't find this post!")

        val postPayload = buildJsonObject {
            put("\$type", CommitType.POST.raw)
            put("text", content)
            put("createdAt", now)
            put("reply", buildJsonObject {
                put("root", buildJsonObject {
                    put("uri", parent.uri)
                    put("cid", parent.cid)
                })
                put("parent", buildJsonObject {
                    put("uri", parent.uri)
                    put("cid", parent.cid)
                })
            })
        }

        val response = client.post {
            url("${BSConstants.BASE_API_URL}/com.atproto.repo.createRecord")
            header(HttpHeaders.ContentType, "application/json")
            header(HttpHeaders.Authorization, "Bearer ${sessionPayload!!.accessToken}")
            setBody(buildJsonObject {
                put("repo", sessionPayload!!.did)
                put("collection", CommitType.POST.raw)
                put("record", postPayload)
            })
        }

        if (response.status.value != 200)
            throw RuntimeException("Couldn't create a post! ${response.bodyAsText()}")
    }

    suspend fun post(content: String) {
        if (sessionPayload == null)
            throw RuntimeException("You can't post something without logging!")

        val now = Instant.now().toString().replace("+00:00", "Z")

        val postPayload = buildJsonObject {
            put("\$type", CommitType.POST.raw)
            put("text", content)
            put("createdAt", now)
        }

        val response = client.post {
            url("${BSConstants.BASE_API_URL}/com.atproto.repo.createRecord")
            header(HttpHeaders.ContentType, "application/json")
            header(HttpHeaders.Authorization, "Bearer ${sessionPayload!!.accessToken}")
            setBody(buildJsonObject {
                put("repo", sessionPayload!!.did)
                put("collection", CommitType.POST.raw)
                put("record", postPayload)
            })
        }

        if (response.status.value != 200)
            throw RuntimeException("Couldn't create a post! Are you logged in?")
    }

    private suspend fun login(): SessionPayload? {
        logger.info { "Logging into BlueSky's API..." }

        return try {
            client.post {
                url("${BSConstants.BASE_API_URL}/com.atproto.server.createSession")
                header(HttpHeaders.ContentType, "application/json")
                setBody(buildJsonObject {
                    put("identifier", identifier)
                    put("password", password)
                })
            }.body()
        } catch (e: Exception) {
            null
        }
    }
}
