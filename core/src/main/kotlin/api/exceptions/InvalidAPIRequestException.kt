package me.hechfx.foreground.core.gateway.api.exceptions

class InvalidAPIRequestException(tryingTo: String, method: String, message: String) : RuntimeException("Cannot send $method from API at $tryingTo! $message")
