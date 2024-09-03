package me.hechfx.foreground.core.gateway.api.entity.post

import kotlinx.serialization.json.*
import me.hechfx.foreground.core.gateway.api.utils.*
import java.time.Instant

class BlueSkyNewPost(
    val text: String,
    val createdAt: String = Instant.now().toString().replace("+00:00", "Z"),
    val reply: Reply? = null,
    val embed: Embed? = null
) {
    class Reply(
        val root: Reference,
        val parent: Reference
    )

    class Embed(
        val type: PostRecordType,
        val record: Reference? = null,
        val images: List<EmbedImage>? = null
    ) {
        class EmbedImage(
            val alt: String? = null,
            val image: InnerEmbedImage? = null
        ) {
            class InnerEmbedImage(
                val ref: String
            )
        }
    }

    class Reference(
        val uri: String,
        val cid: String
    )

    fun toJson() = buildJsonObject {
        put("\$type", ApplicationRepositories.POST.raw)
        put("text", text)
        put("createdAt", createdAt)
        reply?.let {
            put("reply", buildJsonObject {
                put("root", buildJsonObject {
                    put("uri", it.root.uri)
                    put("cid", it.root.cid)
                })
                put("parent", buildJsonObject {
                    put("uri", it.parent.uri)
                    put("cid", it.parent.cid)
                })
            })
        }
        embed?.let {
            put("embed", buildJsonObject {
                put("\$type", it.type.raw)
                it.record?.let { record ->
                    put("record", buildJsonObject {
                        put("uri", record.uri)
                        put("cid", record.cid)
                    })
                }
                it.images?.let { images ->
                    put("images", buildJsonArray {
                        images.forEach { image ->
                            add(buildJsonObject {
                                put("alt", image.alt)
                                put("image", buildJsonObject {
                                    put("ref", buildJsonObject {
                                        put("\$link", image.image?.ref)
                                    })
                                })
                            })
                        }
                    })
                }
            })
        }
    }
}
