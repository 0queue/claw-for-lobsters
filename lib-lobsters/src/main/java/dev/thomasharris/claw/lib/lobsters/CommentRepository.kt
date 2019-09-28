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

//    fun observeComments(storyId: String) =
//        lobstersQueries.getComments(storyId).asFlow().mapToList()
//
//    suspend fun refresh(storyId: String, force: Boolean = false) = withContext(Dispatchers.IO) {
//        val currentComments = lobstersQueries.getComments(storyId).executeAsList()
//        val isOld = currentComments.minBy {
//            it.insertedAt.time
//        }?.let {
//            TimeUnit.MILLISECONDS.toMinutes(Date().time - it.insertedAt.time) >= 60
//        }
//
//        if (currentComments.isNotEmpty() && isOld != true && !force)
//            return@withContext
//
//        val story = lobstersService.getStory(storyId)
//        val now = Date()
//        story.comments?.forEachIndexed { i, c ->
//            lobstersQueries.insertComment(c.toDB(storyId, i, now))
//        }
//    }

//    fun getComments(storyId: String): Pair<StoryDatabaseEntity, List<CommentDatabaseEntity>>? {
//        val storyDb = lobstersQueries.getStory(storyId).executeAsOne()
//        val comments = lobstersQueries.getComments(storyId).executeAsList()
//        val isCommentsOld = comments.minBy {
//            it.insertedAt.time
//        }?.insertedAt?.isOld() ?: true
//
//        if (storyDb.insertedAt.isOld() || isCommentsOld) {
//            val newStory = lobstersService.getStorySync(storyId).execute().body()
//            val now = Date()
//            newStory?.let {
//                lobstersQueries.insertStory(newStory.toDB(storyDb.pageIndex, now))
//                it.comments?.forEachIndexed { i, c ->
//                    lobstersQueries.insertComment(c.toDB(storyId, i, now))
//                }
//            }
//        }
//
//        return Pair(
//            lobstersQueries.getStory(storyId).executeAsOne(),
//            lobstersQueries.getComments(storyId).executeAsList()
//        )
//    }

    fun liveStory(storyId: String) = lobstersQueries.getStory(storyId).asFlow().mapToOne()

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