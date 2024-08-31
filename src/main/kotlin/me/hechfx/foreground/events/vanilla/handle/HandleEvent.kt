package me.hechfx.foreground.events.vanilla.handle

import com.upokecenter.cbor.CBORObject
import me.hechfx.foreground.events.*
import java.time.*

class HandleEvent(raw: CBORObject) : ForegroundEvent() {
    override val INTERNAL_NAME = "#handle"

    val did = raw["did"].AsString()
    val seq = raw["seq"].AsInt32()
    val time = Instant.parse(raw["time"].AsString())
    val handle = raw["handle"].AsString()
}
