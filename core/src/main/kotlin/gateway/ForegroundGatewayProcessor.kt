package me.hechfx.foreground.core.gateway

import com.upokecenter.cbor.CBORObject
import me.hechfx.foreground.*
import me.hechfx.foreground.core.entity.vanilla.user.*
import me.hechfx.foreground.core.events.*
import me.hechfx.foreground.core.events.vanilla.account.*
import me.hechfx.foreground.core.events.vanilla.commit.*
import me.hechfx.foreground.core.events.vanilla.handle.*
import me.hechfx.foreground.core.events.vanilla.identity.*
import me.hechfx.foreground.core.events.vanilla.tombstone.*
import me.hechfx.foreground.core.utils.*
import java.io.InputStream

class ForegroundGatewayProcessor(val client: ForegroundWebSocketClient) {
    suspend fun process(raw: String, obj: CBORObject) {
        when (ForegroundEventType.from(raw)) {
            /**
             * Fired when the user do something in the platform, like a post, like, follow, etc.
             */
            ForegroundEventType.COMMIT -> {
                client.events.emit(
                    CommitEvent(obj)
                )
            }
            /**
             * I haven't figured out what this event is for, but it's related to the user's identity.
             */
            ForegroundEventType.IDENTITY -> {
                client.events.emit(
                    IdentityEvent(obj)
                )
            }

            /**
             * I haven't figured out what this event is for, but it's related to the user's account.
             */
            ForegroundEventType.ACCOUNT -> {
                client.events.emit(
                    AccountEvent(obj)
                )
            }

            /**
             *  I haven't figured out what this event is for, but it's related to the user's handle.
             */
            ForegroundEventType.HANDLE -> {
                client.events.emit(
                    HandleEvent(obj)
                )
            }

            /**
             * What the hell is tombstone?
             */
            ForegroundEventType.TOMBSTONE -> {
                client.events.emit(
                    TombstoneEvent(obj)
                )
            }

            null -> TODO()
        }
    }
}
