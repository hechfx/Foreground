package me.hechfx.foreground.core.events.vanilla.account

import com.upokecenter.cbor.CBORObject
import me.hechfx.foreground.core.events.*
import java.time.*

class AccountEvent(raw: CBORObject) : BlueSkyEvent() {
    override val INTERNAL_NAME = "#account"

    val did = raw["did"].AsString()
    val seq = raw["seq"].AsInt64Value()
    val time = Instant.parse(raw["time"].AsString())
    val active = raw["active"].AsBoolean()
}
