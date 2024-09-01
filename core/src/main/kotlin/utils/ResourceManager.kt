package me.hechfx.foreground.core.utils

import com.github.benmanes.caffeine.cache.*
import me.hechfx.foreground.core.entity.vanilla.user.*
import java.util.concurrent.*

object ResourceManager {
    val users = Caffeine
        .newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build<String, User>()
        .asMap()
}
