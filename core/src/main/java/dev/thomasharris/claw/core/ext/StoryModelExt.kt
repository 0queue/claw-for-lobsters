package dev.thomasharris.claw.core.ext

import android.content.Context
import dev.thomasharris.claw.core.R
import dev.thomasharris.claw.lib.lobsters.StoryModel
import java.net.URI
import java.util.Date
import java.util.concurrent.TimeUnit
import kotlin.math.abs

fun Date.postedAgo() = abs(Date().time - time).let { t ->
    listOf(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES).map {
        Pair(it.convert(t, TimeUnit.MILLISECONDS), it)
    }.find { it.first > 0 } ?: Pair(0L, TimeUnit.MINUTES)
}

fun Pair<Long, TimeUnit>.toString(context: Context): String {
    val t = first.toInt()
    return when (val unit = second) {
        TimeUnit.DAYS -> context.resources.getQuantityString(R.plurals.numberOfDays, t, t)
        TimeUnit.HOURS -> context.resources.getQuantityString(R.plurals.numberOfHours, t, t)
        TimeUnit.MINUTES -> context.resources.getQuantityString(
            R.plurals.numberOfMinutes,
            t,
            t
        )
        else -> throw IllegalStateException("Invalid TimeUnit: $unit")
    }
}

fun StoryModel.shortUrl() = URI(url.trim()).host?.removePrefix("www.")