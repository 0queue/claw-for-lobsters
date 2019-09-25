package dev.thomasharris.lib.lobsters

import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class StoryRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val storyQueries: StoryDatabaseEntityQueries
) {

    // use a callback instead of return value so that when
    // a new page is fetched the stories can be inserted
    // after sending them back to the paging library
    fun getPageSync(index: Int, callback: (List<Story>) -> Unit) {

        // get offline page
        val dbPage = storyQueries.getPage(index.toLong()).executeAsList()

        // check age
        val isOld: Boolean? = dbPage.minBy {
            it.insertedAt.time
        }?.insertedAt?.let {
            TimeUnit.MILLISECONDS.toMinutes(Date().time - it.time) >= 60
        }

        // if page not downloaded, or is old...
        if (dbPage.isEmpty() || isOld == true) {
            // fetch new page
            val newPage =
                lobstersService.getPageSync(index + 1).execute().body()?.map(Story.Companion::from)
                    ?: throw NullPointerException("TODO")

            // return new page
            callback(newPage)

            // store new page
            val now = Date()
            newPage.forEach {
                storyQueries.insertStory(it.toDatabaseEntity(index, now))
            }
        } else
            callback(dbPage.map(Story.Companion::from))
    }

    fun invalidate() {
        storyQueries.clear()
    }
}

data class Story(
    val shortId: String,
    val createdAt: Date,
    val title: String,
    val url: String,
    val score: Int,
    val upvotes: Int,
    val downvotes: Int,
    val commentCount: Int,
    val description: String,
    val submitterUsername: String,
    val tags: List<String>
) {

    fun toDatabaseEntity(index: Int, insertedAt: Date = Date()): StoryDatabaseEntity {
        return StoryDatabaseEntity.Impl(
            shortId,
            title,
            createdAt,
            url,
            score.toLong(),
            upvotes.toLong(),
            downvotes.toLong(),
            commentCount.toLong(),
            description,
            submitterUsername,
            tags,
            index.toLong(),
            insertedAt
        )
    }

    companion object {
        fun from(storyNetworkEntity: StoryNetworkEntity): Story {
            with(storyNetworkEntity) {
                return Story(
                    shortId,
                    createdAt,
                    title,
                    url,
                    score,
                    upvotes,
                    downvotes,
                    commentCount,
                    description,
                    submitter.username,
                    tags
                )
            }
        }

        fun from(storyDatabaseEntity: StoryDatabaseEntity): Story {
            with(storyDatabaseEntity) {
                return Story(
                    shortId,
                    createdAt,
                    title,
                    url,
                    score.toInt(),
                    upvotes.toInt(),
                    downvotes.toInt(),
                    commentCount.toInt(),
                    description,
                    submitterUsername,
                    tags
                )
            }
        }
    }
}