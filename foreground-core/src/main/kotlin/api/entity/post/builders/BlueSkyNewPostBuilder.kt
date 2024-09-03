package me.hechfx.foreground.core.gateway.api.entity.post.builders

import me.hechfx.foreground.core.gateway.api.entity.post.BlueSkyNewPost
import me.hechfx.foreground.core.gateway.api.utils.*

class BlueSkyNewPostBuilder(
    var text: String = "",
    var reply: BlueSkyNewPost.Reply? = null,
    var embed: EmbedBuilder? = null
) {
    fun build(): BlueSkyNewPost {
        require(text.isNotBlank()) { "Text must not be blank" }
        require(text.length <= 300) { "Text must not exceed 300 characters" }

        return BlueSkyNewPost(
            text = text,
            reply = reply,
            embed = embed?.build()
        )
    }

    fun replyTo(cid: String, uri: String) {
        reply = BlueSkyNewPost.Reply(
            root = BlueSkyNewPost.Reference(
                cid = cid,
                uri = uri
            ),
            parent = BlueSkyNewPost.Reference(
                cid = cid,
                uri = uri
            )
        )
    }

    class EmbedBuilder(
        var type: PostRecordType = PostRecordType.QUOTE,
        var record: ReferenceBuilder? = null,
        var images: MutableList<EmbedImageBuilder>? = null
    ) {
        class EmbedImageBuilder(
            var alt: String? = null,
            var image: InnerEmbedImageBuilder? = null
        ) {
            class InnerEmbedImageBuilder(
                var ref: String
            ) {
                fun build() = BlueSkyNewPost.Embed.EmbedImage.InnerEmbedImage(
                    ref = ref
                )
            }

            fun build() = BlueSkyNewPost.Embed.EmbedImage(
                alt = alt,
                image = image?.build()
            )
        }

        fun new(builder: EmbedImageBuilder.() -> Unit) {
            val dec = EmbedImageBuilder().apply(builder)

            images?.add(dec)
        }

        fun build() = BlueSkyNewPost.Embed(
            type = type,
            record = record?.build()
        )
    }

    class ReplyBuilder(
        var root: ReferenceBuilder? = null,
        var parent: ReferenceBuilder? = null
    ) {
        fun reference(builder: ReferenceBuilder.() -> Unit) {
            root = ReferenceBuilder().apply(builder)
            parent = root
        }

        fun build() = BlueSkyNewPost.Reply(
            root = root!!.build(),
            parent = parent!!.build()
        )
    }

    class ReferenceBuilder(
        var uri: String = "",
        var cid: String = ""
    ) {
        fun build() = BlueSkyNewPost.Reference(
            uri = uri,
            cid = cid
        )
    }
}
