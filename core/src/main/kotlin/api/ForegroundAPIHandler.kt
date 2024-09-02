package me.hechfx.foreground.core.gateway.api

import io.github.oshai.kotlinlogging.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.*
import me.hechfx.foreground.core.gateway.*
import me.hechfx.foreground.core.gateway.api.builders.*
import me.hechfx.foreground.core.gateway.api.entity.*
import me.hechfx.foreground.core.gateway.api.exceptions.*
import me.hechfx.foreground.core.gateway.api.payloads.*
import me.hechfx.foreground.core.gateway.api.utils.*
import me.hechfx.foreground.core.utils.*

class ForegroundAPIHandler(val m: ForegroundWebSocketClient) {
    companion object {
        private val logger = KotlinLogging.logger {}
    }

    /**
     * Create a new session to the BlueSky's API; Giving access to auth required lexicons.
     * @param identifier The user's identifier (e-mail or @)
     * @param password The user's password
     */
    suspend fun createSession(identifier: String, password: String): Session? {
        val response = request<SessionPayload>(
            method = HttpMethod.Post,
            lexicon = APILexicons.CREATE_SESSION,
            body = buildJsonObject {
                put("identifier", identifier)
                put("password", password)
            }.toString()
        )

        return if (response != null) {
            m.bsSession = Session(response)

            m.bsSession!!
        } else {
            null
        }
    }

    /**
     * Create a new post (needs auth)
     * @param builder The builder to create a new post
     */
    suspend fun createPost(builder: NewPostBuilder.() -> Unit) {
        if (m.bsSession == null) throw InvalidAPIRequestException("You need to create a session first!", APILexicons.SEND_POST.raw, "")

        val newPost = NewPostBuilder().apply(builder).build()

        val requestPayload = buildJsonObject {
            put("repo", m.bsSession!!.did)
            put("collection", ApplicationRepositories.POST.raw)
            put("record", newPost.toJson())
        }

        request<Any>(
            HttpMethod.Post,
            APILexicons.SEND_POST,
            auth = true,
            body = requestPayload.toString()
        )
    }

    /**
     * Retrieves a post by its URI
     * @param uri The URI of the post (e.g: at://did/collection/rkey)
     * @return The post entity
     */
    suspend fun retrievePostByURI(uri: String): Post? {
        val begin = uri.substring("at://".length)
        val author = begin.split("/")[0]
        val collection = begin.split("/")[1]
        val rKey = begin.split("/")[2]

        println(uri)

        val params = mapOf(
            "repo" to author,
            "collection" to collection,
            "rkey" to rKey
        )

        val response: PostPayload? = request(
            HttpMethod.Get,
            APILexicons.RETRIEVE_POST,
            params = params,
        )

        return if (response != null) {
            Post(response)
        } else {
            null
        }
    }

    /**
     * Sends a request o BlueSky's API; If you need auth, you can set the parameter to true
     * Also, you can specify which payload you want to receive.
     */
    private suspend inline fun <reified T : Any> request(
        method: HttpMethod,
        lexicon: APILexicons,
        auth: Boolean = false,
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        body: Any? = null
    ): T? {
        lateinit var bodyError: String

        return try {
            val response = m.client.request {
                this.method = method

                if (body != null) {
                    header(HttpHeaders.ContentType, "application/json")
                    setBody(body)
                }

                if (auth)
                    header(HttpHeaders.Authorization, "Bearer ${m.bsSession?.accessToken}")

                headers?.forEach { (t, u) ->
                    this.headers { append(t, u) }
                }

                url {
                    takeFrom("${BSConstants.BASE_API_URL}/${lexicon.raw}")
                    params?.forEach { (t, u) ->
                        parameters.append(t, u)
                    }
                }
            }

            bodyError = response.bodyAsText()

            response.body<T>()
        } catch (e: Exception) {
            logger.warn(e) { "Error while requesting to BlueSky's API; $bodyError" }
            null
        }
    }
}
