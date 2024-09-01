package me.hechfx.foreground.core.events.vanilla.commit

import com.upokecenter.cbor.CBORObject
import me.hechfx.foreground.core.events.*
import me.hechfx.foreground.core.events.vanilla.commit.content.*
import me.hechfx.foreground.core.utils.YokyeUtils.parseCAR
import java.time.*


class CommitEvent(raw: CBORObject) : ForegroundEvent() {
    override val INTERNAL_NAME = "#commit"

    @OptIn(ExperimentalStdlibApi::class)
    val properties = raw["ops"]?.values?.map {
        val cidAsBA = try {
            it["cid"].EncodeToBytes()
        } catch (e: IllegalStateException) {
            null
        }

        val cidAsHex = cidAsBA?.toHexString()
        val type = CommitType.from((it["path"].AsString()).split("/")[0])
        val path = it["path"].AsString().split("/")[1]
        val action = it["action"].AsString()

        Properties(cidAsHex, type, path, action)
    }

    val rev = raw["rev"]?.AsString()
    val seq = raw["seq"]?.AsInt32()
    val author = raw["repo"]?.AsString()
    val time = raw["time"]?.AsString()?.let { Instant.parse(it) }

    val content: List<ContentType?> = parseCAR(raw["blocks"].GetByteString().inputStream()).map {
        if (it["\$type"] == null) return@map null

        when (CommitType.fromOrNull(it["\$type"].AsString())) {
            CommitType.POST -> ContentType.PostContent(it)
            CommitType.FOLLOW -> ContentType.FollowContent(it)
            CommitType.LIKE -> ContentType.LikeContent(it)
            else -> null
        }
    }

    class Properties(
        val cid: String?,
        val type: CommitType,
        val path: String,
        val action: String
    ) {
        override fun toString(): String {
            return "Properties(type=$type, path=$path, action=$action)"
        }
    }
}
