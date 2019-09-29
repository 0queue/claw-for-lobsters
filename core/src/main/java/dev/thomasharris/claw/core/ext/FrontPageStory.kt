package dev.thomasharris.claw.core.ext

import dev.thomasharris.claw.lib.lobsters.FrontPageStory
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

fun FrontPageStory.postedAgo() = abs(Date().time - createdAt.time).let { t ->
    listOf(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES).map {
        Pair(it.convert(t, TimeUnit.MILLISECONDS), it)
    }.find { it.first > 0 } ?: Pair(0L, TimeUnit.MINUTES)
}

fun FrontPageStory.shortUrl() = URI(url).host?.removePrefix("www.")