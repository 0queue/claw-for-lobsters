package dev.thomasharris.claw.feature.frontpage

import dev.thomasharris.claw.core.ext.postedAgo
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
