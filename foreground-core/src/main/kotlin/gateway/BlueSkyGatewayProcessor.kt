package me.hechfx.foreground.core.gateway

import com.upokecenter.cbor.CBORObject
import me.hechfx.foreground.core.events.*
import me.hechfx.foreground.core.events.vanilla.account.*
import me.hechfx.foreground.core.events.vanilla.commit.*
import me.hechfx.foreground.core.events.vanilla.handle.*
import me.hechfx.foreground.core.events.vanilla.identity.*
import me.hechfx.foreground.core.events.vanilla.tombstone.*

class BlueSkyGatewayProcessor(private val m: ForegroundClient) {
    suspend fun process(raw: String, obj: CBORObject) {
        when (BlueSkyEventType.from(raw)) {
            /**
             * Fired when the user do something in the platform, like a post, like, follow, etc.
             */
            BlueSkyEventType.COMMIT -> {
                m.events.emit(
                    CommitEvent(obj)
                )
            }
            /**
             * I haven't figured out what this event is for, but it's related to the user's identity.
             */
            BlueSkyEventType.IDENTITY -> {
                m.events.emit(
                    IdentityEvent(obj)
                )
            }

            /**
             * I haven't figured out what this event is for, but it's related to the user's account.
             */
            BlueSkyEventType.ACCOUNT -> {
                m.events.emit(
                    AccountEvent(obj)
                )
            }

            /**
             *  I haven't figured out what this event is for, but it's related to the user's handle.
             */
            BlueSkyEventType.HANDLE -> {
                m.events.emit(
                    HandleEvent(obj)
                )
            }

            /**
             * What the hell is tombstone?
             */
            BlueSkyEventType.TOMBSTONE -> {
                m.events.emit(
                    TombstoneEvent(obj)
                )
            }

            null -> {}
        }
    }
}
