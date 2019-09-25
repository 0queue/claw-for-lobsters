package dev.thomasharris.feature.frontpage

import dev.thomasharris.lib.lobsters.Story
import java.net.URI
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

sealed class FrontPageItem

data class FrontPageStory(
    val shortId: String,
    val title: String,
    val username: String,
    val postedAgo: Pair<Long, TimeUnit>,
    val numComments: Int,
    val voteTotal: Int,
    val shortURL: String?,
    val tags: List<FrontPageTag>
) : FrontPageItem()

data class FrontPageDivider(val n: Int) : FrontPageItem()

fun Story.frontPage(): FrontPageStory {
    val postedAgo = abs(Date().time - createdAt.time).let { t ->
        listOf(TimeUnit.DAYS, TimeUnit.HOURS, TimeUnit.MINUTES).map {
            Pair(it.convert(t, TimeUnit.MILLISECONDS), it)
        }.find { it.first > 0 } ?: Pair(0L, TimeUnit.MINUTES)
    }

    val frontPageTags = tags.map {
        FrontPageTag(it.tag, it.isMedia)
    }

    val shortURL = URI(url).host?.removePrefix("www.")

    return FrontPageStory(
        shortId,
        title,
        submitterUsername,
        postedAgo,
        commentCount,
        upvotes - downvotes,
        shortURL,
        frontPageTags
    )
}

data class FrontPageTag(val tag: String, val isMedia: Boolean)