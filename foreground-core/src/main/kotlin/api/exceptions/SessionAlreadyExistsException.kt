package me.hechfx.foreground.core.gateway.api.exceptions

class SessionAlreadyExistsException(message: String?) : RuntimeException(message) {
    constructor(message: Throwable) : this(message.message)
}
