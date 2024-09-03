package me.hechfx.foreground.core.gateway.api

import io.github.oshai.kotlinlogging.*
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.*
import me.hechfx.foreground.core.gateway.*
import me.hechfx.foreground.core.gateway.api.entity.post.BlueSkyPost
import me.hechfx.foreground.core.gateway.api.entity.post.builders.BlueSkyNewPostBuilder
import me.hechfx.foreground.core.gateway.api.entity.session.BlueSkySession
import me.hechfx.foreground.core.gateway.api.exceptions.*
import me.hechfx.foreground.core.gateway.api.utils.*
import me.hechfx.foreground.core.utils.*

class BlueSkyAPIWrapper(private val m: ForegroundClient) {
    companion object {
        private val logger = KotlinLogging.logger {}
        private val client = HttpClient(CIO) {
            install(ContentNegotiation) {
                json(json = Json {
                    ignoreUnknownKeys = true
                })
            }
        }
    }

    /**
     * Create a new session to the BlueSky's API; Giving access to auth required lexicons.
     * @param identifier The user's identifier (e-mail or @)
     * @param password The user's password
     */
    suspend fun createSession(identifier: String, password: String): BlueSkySession? {
        val response = try {
            request<BlueSkySession>(
                method = HttpMethod.Post,
                lexicon = BlueSkyAPILexicons.CREATE_SESSION,
                body = buildJsonObject {
                    put("identifier", identifier)
                    put("password", password)
                }.toString()
            )
        } catch (e: Exception) {
            logger.warn(e) { "Error while creating a session to BlueSky's API" }
            null
        }

        return if (response != null) {
            m.blueSkySession = response

            m.blueSkySession!!
        } else {
            null
        }
    }

    /**
     * Create a new post (needs auth)
     * @param builder The builder to create a new post
     */
    suspend fun createPost(builder: BlueSkyNewPostBuilder.() -> Unit) {
        if (m.blueSkySession == null) throw InvalidAPIRequestException("You need to create a session first!", BlueSkyAPILexicons.SEND_POST.raw, "")

        val newPost = BlueSkyNewPostBuilder().apply(builder).build()

        val requestPayload = buildJsonObject {
            put("repo", m.blueSkySession!!.did)
            put("collection", ApplicationRepositories.POST.raw)
            put("record", newPost.toJson())
        }

        request<Any>(
            HttpMethod.Post,
            BlueSkyAPILexicons.SEND_POST,
            auth = true,
            body = requestPayload.toString()
        )
    }

    /**
     * Retrieves a post by its URI
     * @param uri The URI of the post (e.g: at://did/collection/rkey)
     * @return The post entity
     */
    suspend fun retrievePostByURI(uri: String): BlueSkyPost? {
        val begin = uri.substring("at://".length)
        val author = begin.split("/")[0]
        val collection = begin.split("/")[1]
        val rKey = begin.split("/")[2]

        val params = mapOf(
            "repo" to author,
            "collection" to collection,
            "rkey" to rKey
        )

        val response: BlueSkyPost? = request(
            HttpMethod.Get,
            BlueSkyAPILexicons.RETRIEVE_POST,
            params = params,
        )

        return response
    }

    /**
     * Sends a request o BlueSky's API; If you need auth, you can set the parameter to true
     * Also, you can specify which payload you want to receive.
     */
    private suspend inline fun <reified T : Any> request(
        method: HttpMethod,
        lexicon: BlueSkyAPILexicons,
        auth: Boolean = false,
        params: Map<String, String>? = null,
        headers: Map<String, String>? = null,
        body: Any? = null
    ): T? {
        lateinit var bodyError: String

        return try {
            val response = client.request {
                this.method = method

                if (body != null) {
                    header(HttpHeaders.ContentType, "application/json")
                    setBody(body)
                }

                if (auth && m.blueSkySession != null)
                    header(HttpHeaders.Authorization, "Bearer ${m.blueSkySession!!.accessToken}")

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
            logger.error(e) { "Error while requesting to BlueSky's API; $bodyError" }
            null
        }
    }
}
