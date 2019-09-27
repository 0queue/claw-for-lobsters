package dev.thomasharris.claw.feature.frontpage

import dev.thomasharris.claw.lib.lobsters.FrontPageStory
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

sealed class FrontPageItem {
    data class Story(
        val shortId: String,
        val title: String,
        val submitterUsername: String,
        val avatarShortUrl: String,
        val createdAt: Date,
        val commentCount: Int,
        val score: Int,
        val url: String,
        val postedAgo: Pair<Long, TimeUnit>,
        val tags: List<Pair<String, Boolean>>
    ) : FrontPageItem() {
        fun shortUrl() = URI(url).host?.removePrefix("www.")
    }

    data class Divider(val n: Int) : FrontPageItem()
}

private fun FrontPageStory.postedAgo() = abs(Date().time - createdAt.time).let { t ->
    listOf(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES).map {
        Pair(it.convert(t, TimeUnit.MILLISECONDS), it)
    }.find { it.first > 0 } ?: Pair(0L, TimeUnit.MINUTES)
}

fun FrontPageStory.toItem(tags: List<Pair<String, Boolean>>) =
    FrontPageItem.Story(
        shortId,
        title,
        submitterUsername,
        avatarShortUrl,
        createdAt,
        commentCount,
        score,
        url,
        postedAgo(),
        tags
    )
