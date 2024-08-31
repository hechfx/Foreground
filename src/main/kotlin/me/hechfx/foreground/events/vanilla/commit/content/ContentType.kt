package me.hechfx.foreground.events.vanilla.commit.content

import com.upokecenter.cbor.*
import me.hechfx.foreground.utils.*
import java.time.*

sealed class ContentType {
    class PostContent(raw: CBORObject) : ContentType() {
        val text = raw["text"]?.AsString()
        val langs = raw["langs"]?.values?.map { it.AsString() }
        val createdAt = Instant.parse(raw["createdAt"].AsString())
    }

    class FollowContent(raw: CBORObject) : ContentType() {
        val subject = raw["subject"].AsString()
        val createdAt = Instant.parse(raw["createdAt"].AsString())
        val subjectURI = "${BSConstants.BASE_URL}/profile/$subject"
    }

    class LikeContent(raw: CBORObject) : ContentType() {
        val subject = Subject(
            raw["subject"]["cid"].AsString(),
            raw["subject"]["uri"].AsString()
        )

        val createdAt = Instant.parse(raw["createdAt"].AsString())

        data class Subject(
            val cid: String,
            val uri: String
        ) {
            fun toURI(): String {
                val user = uri.substring("at://".length).split("/")[0]
                val post = uri.substring("at://".length).split("/")[2]

                return "${BSConstants.BASE_URL}/profile/$user/post/$post"
            }
        }
    }
}
