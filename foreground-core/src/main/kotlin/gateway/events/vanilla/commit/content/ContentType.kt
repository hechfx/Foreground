package me.hechfx.foreground.core.events.vanilla.commit.content

import com.upokecenter.cbor.*
import me.hechfx.foreground.core.utils.*
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
            val uri: String,
            val prettyURI: String = "${BSConstants.BASE_URL}/profile/${uri.substring("at://".length).split("/")[0]}/post/${uri.substring("at://".length).split("/")[2]}"
        )
    }

    class RepostContent(raw: CBORObject) : ContentType()

    class ProfileContent(raw: CBORObject) : ContentType()

    class ChatDeclarationContent(raw: CBORObject) : ContentType()

    class BlockContent(raw: CBORObject) : ContentType()

    class ListItemContent(raw: CBORObject) : ContentType()

    class ListBlockContent(raw: CBORObject) : ContentType()

    class StarterPackContent(raw: CBORObject) : ContentType()

    class PostGateContent(raw: CBORObject) : ContentType()

    class ThreadGateContent(raw: CBORObject) : ContentType()

    class ListContent(raw: CBORObject) : ContentType()

    class DeclarationContent(raw: CBORObject) : ContentType()

    class GeneratorContent(raw: CBORObject) : ContentType()

    class EmbedRecordContent(raw: CBORObject) : ContentType()

    class BlogEntryContent(raw: CBORObject) : ContentType()

    class LabelerServiceContent(raw: CBORObject) : ContentType()

    class TODOContent(raw: CBORObject) : ContentType()
}
