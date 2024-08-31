package me.hechfx.foreground.events.vanilla.identity

import com.upokecenter.cbor.CBORObject
import me.hechfx.foreground.events.*

class IdentityEvent(raw: CBORObject) : ForegroundEvent() {
    override val INTERNAL_NAME = "#identity"

    val did = raw["did"].AsString()
    val seq = raw["seq"].AsInt32()
    val handle = raw["handle"].AsString()
}
