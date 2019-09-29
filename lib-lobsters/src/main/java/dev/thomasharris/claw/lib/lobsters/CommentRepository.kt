package dev.thomasharris.claw.lib.lobsters

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class CommentRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val lobstersQueries: LobstersQueries,
    private val background: Executor
) {

    fun liveStory(storyId: String) = lobstersQueries.getFrontPageStory(storyId).asFlow().mapToOne()

    fun liveComments(storyId: String) = lobstersQueries.getComments(storyId).asFlow().mapToList()

    fun refresh(storyId: String, force: Boolean = false) {
        background.execute {
            val storyDb = lobstersQueries.getStory(storyId).executeAsOne()

            val shouldRefresh = if (force) true else {
                val comments = lobstersQueries.getComments(storyId).executeAsList()
                storyDb.insertedAt.isOld() || (comments.minBy {
                    it.insertedAt.time
                }?.insertedAt?.isOld() ?: true)
            }

            if (!shouldRefresh)
                return@execute

            val newStory = lobstersService.getStorySync(storyId).execute().body()

            newStory?.let { s ->
                val now = Date()
                lobstersQueries.insertStory(s.toDB(storyDb.pageIndex, now))
                s.comments?.forEachIndexed { i, c ->
                    lobstersQueries.insertUser(c.commentingUser.toDB(now))
                    lobstersQueries.insertComment(c.toDB(storyId, i, now))
                }
            }
        }
    }
}

fun CommentNetworkEntity.toDB(
    storyId: String,
    index: Int,
    insertedAt: Date
) = CommentDatabaseEntity.Impl(
    shortId,
    storyId,
    index,
    shortIdUrl,
    createdAt,
    updatedAt,
    isDeleted,
    isModerated,
    score,
    upvotes,
    downvotes,
    comment,
    indentLevel,
    commentingUser.username,
    insertedAt
)

fun Date.isOld(): Boolean {
    return TimeUnit.MILLISECONDS.toMinutes(Date().time - time) >= 60
}