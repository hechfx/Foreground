package me.hechfx.foreground.core.gateway.utils.builders

class ForegroundClientOptionsBuilder(
    var identifier: String? = null,
    var password: String? = null,
) {
    fun build() = ForegroundClientOptions(
        identifier = identifier!!,
        password = password!!
    )
}