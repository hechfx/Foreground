package me.hechfx.foreground.core.events.vanilla.tombstone

import com.upokecenter.cbor.CBORObject
import me.hechfx.foreground.core.events.*

class TombstoneEvent(raw: CBORObject) : BlueSkyEvent() {
    override val INTERNAL_NAME = "#tombstone"

    val did = raw["did"].AsString()
    val seq = raw["seq"].AsInt32()
    val time = raw["time"].AsString()
}
