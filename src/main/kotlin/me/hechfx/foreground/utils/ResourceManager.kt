package me.hechfx.foreground.utils

import com.github.benmanes.caffeine.cache.*
import me.hechfx.foreground.entity.vanilla.user.*
import java.util.concurrent.*

object ResourceManager {
    val users = Caffeine
        .newBuilder()
        .expireAfterAccess(1, TimeUnit.HOURS)
        .build<String, User>()
        .asMap()
}
