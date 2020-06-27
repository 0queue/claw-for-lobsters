package dev.thomasharris.claw.lib.lobsters

import java.net.URI
import java.util.Date

data class StoryWithTagsModel(
    val shortId: String,
    val title: String,
    val username: String,
    val avatarShortUrl: String,
    val createdAt: Date,
    val commentCount: Int,
    val score: Int,
    val url: String,
    val tags: List<TagModel>,
    val pageIndex: Int,
    val pageSubIndex: Int,
    val description: String
)

fun StoryWithTagsModel.shortUrl() = URI(url.trim()).host?.removePrefix("www.")

infix fun StoryModel.x(tagMap: Map<String, TagModel>) =
    this x tags.map { tagMap[it] ?: TagModel(it, false) }

infix fun StoryModel.x(tags: List<TagModel>) =
    StoryWithTagsModel(
        shortId = shortId,
        title = title,
        username = username,
        avatarShortUrl = avatarShortUrl,
        createdAt = createdAt,
        commentCount = commentCount,
        score = score,
        url = url,
        tags = tags,
        pageIndex = pageIndex,
        pageSubIndex = pageSubIndex,
        description = description
    )