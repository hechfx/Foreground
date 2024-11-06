package me.hechfx.foreground.core.events.vanilla.identity

import com.upokecenter.cbor.CBORObject
import me.hechfx.foreground.core.events.*

class IdentityEvent(raw: CBORObject) : BlueSkyEvent() {
    override val INTERNAL_NAME = "#identity"

    val did = raw["did"].AsString()
    val seq = raw["seq"].AsInt64Value()
    val handle = raw["handle"].AsString()
}
