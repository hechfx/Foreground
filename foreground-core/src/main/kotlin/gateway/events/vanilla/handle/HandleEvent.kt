package me.hechfx.foreground.core.events.vanilla.handle

import com.upokecenter.cbor.CBORObject
import me.hechfx.foreground.core.events.*
import java.time.*

class HandleEvent(raw: CBORObject) : BlueSkyEvent() {
    override val INTERNAL_NAME = "#handle"

    val did = raw["did"].AsString()
    val seq = raw["seq"].AsInt64Value()
    val time = Instant.parse(raw["time"].AsString())
    val handle = raw["handle"].AsString()
}
