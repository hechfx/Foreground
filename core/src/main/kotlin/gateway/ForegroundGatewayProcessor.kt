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
    suspend fun process(raw: String, inputStream: InputStream) {
        lateinit var body: CBORObject

        val users = ResourceManager.users

        when (ForegroundEventType.from(raw)) {
            /**
             * Fired when the user do something in the platform, like a post, like, follow, etc.
             */
            ForegroundEventType.COMMIT -> {
                body = CBORObject.Read(inputStream)

                client.events.emit(CommitEvent(body))
            }
            /**
             * I haven't figured out what this event is for, but it's related to the user's identity.
             */
            ForegroundEventType.IDENTITY -> {
                body = CBORObject.Read(inputStream)

                val parsed = IdentityEvent(body)

                if (!users.containsKey(parsed.did)) {
                    users[parsed.did] = User(
                        parsed.did,
                        parsed.handle
                    )
                }

                client.events.emit(parsed)
            }

            /**
             * I haven't figured out what this event is for, but it's related to the user's account.
             */
            ForegroundEventType.ACCOUNT -> {
                body = CBORObject.Read(inputStream)

                client.events.emit(AccountEvent(body))
            }

            /**
             *  I haven't figured out what this event is for, but it's related to the user's handle.
             */
            ForegroundEventType.HANDLE -> {
                body = CBORObject.Read(inputStream)

                val parsed = HandleEvent(body)

                if (!users.containsKey(parsed.did)) {
                    users[parsed.did] = User(
                        parsed.did,
                        parsed.handle
                    )
                }

                client.events.emit(HandleEvent(body))
            }

            /**
             * What the hell is tombstone?
             */
            ForegroundEventType.TOMBSTONE -> {
                body = CBORObject.Read(inputStream)

                client.events.emit(TombstoneEvent(body))
            }
        }
    }
}
