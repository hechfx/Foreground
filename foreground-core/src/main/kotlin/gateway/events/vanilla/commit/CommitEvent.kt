package me.hechfx.foreground.core.events.vanilla.commit

import com.upokecenter.cbor.CBORObject
import me.hechfx.foreground.core.events.*
import me.hechfx.foreground.core.events.vanilla.commit.content.*
import me.hechfx.foreground.core.gateway.api.utils.*
import me.hechfx.foreground.core.utils.YokyeUtils.parseCAR
import java.time.*

class CommitEvent(raw: CBORObject) : BlueSkyEvent() {
    override val INTERNAL_NAME = "#commit"

    @OptIn(ExperimentalStdlibApi::class)
    val properties = raw["ops"]?.values?.map {
        val cidAsBA = try {
            it["cid"].EncodeToBytes()
        } catch (e: IllegalStateException) {
            null
        }

        val cidAsHex = cidAsBA?.toHexString()
        val type = ApplicationRepositories.from((it["path"].AsString()).split("/")[0])
        val path = it["path"].AsString().split("/")[1]
        val action = it["action"].AsString()

        Properties(cidAsHex, type, path, action)
    }

    val rev = raw["rev"]?.AsString()
    val seq = raw["seq"]?.AsInt64Value()
    val author = raw["repo"]?.AsString()
    val time = raw["time"]?.AsString()?.let { Instant.parse(it) }

    private var blocks = raw["blocks"]?.GetByteString()?.inputStream()

    val content: ContentType? = parseCAR(blocks).mapNotNull {
        if (it["\$type"] == null) return@mapNotNull null

        when (ApplicationRepositories.from(it["\$type"].AsString())) {
            ApplicationRepositories.POST -> ContentType.PostContent(it)
            ApplicationRepositories.FOLLOW -> ContentType.FollowContent(it)
            ApplicationRepositories.LIKE -> ContentType.LikeContent(it)
            ApplicationRepositories.REPOST -> ContentType.RepostContent(it)
            ApplicationRepositories.PROFILE -> ContentType.ProfileContent(it)
            ApplicationRepositories.CHAT_DECLARATION -> ContentType.ChatDeclarationContent(it)
            ApplicationRepositories.BLOCK -> ContentType.BlockContent(it)
            ApplicationRepositories.LIST_ITEM -> ContentType.ListItemContent(it)
            ApplicationRepositories.LIST_BLOCK -> ContentType.ListBlockContent(it)
            ApplicationRepositories.STARTER_PACK -> ContentType.StarterPackContent(it)
            ApplicationRepositories.POST_GATE -> ContentType.PostGateContent(it)
            ApplicationRepositories.THREAD_GATE -> ContentType.ThreadGateContent(it)
            ApplicationRepositories.LIST -> ContentType.ListContent(it)
            ApplicationRepositories.DECLARATION -> ContentType.DeclarationContent(it)
            ApplicationRepositories.GENERATOR -> ContentType.GeneratorContent(it)
            ApplicationRepositories.EMBED_RECORD -> ContentType.EmbedRecordContent(it)
            ApplicationRepositories.BLOG_ENTRY -> ContentType.BlogEntryContent(it)
            ApplicationRepositories.LABELER_SERVICE -> ContentType.LabelerServiceContent(it)
            ApplicationRepositories.POUCH_LINK -> ContentType.TODOContent(it)
            null -> null
        }
    }.firstOrNull()

    class Properties(
        val cid: String?,
        val type: ApplicationRepositories?,
        val path: String,
        val action: String
    ) {
        override fun toString(): String {
            return "Properties(type=$type, path=$path, action=$action)"
        }
    }
}
