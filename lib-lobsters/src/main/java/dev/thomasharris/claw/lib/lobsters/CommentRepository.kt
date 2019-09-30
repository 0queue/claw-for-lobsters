package dev.thomasharris.claw.lib.lobsters

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import com.squareup.sqldelight.runtime.coroutines.mapToOne
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import java.util.*
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val lobstersQueries: LobstersQueries,
    private val background: Executor
) {

    @ExperimentalCoroutinesApi
    private val statusChannel = BroadcastChannel<LoadingStatus>(CONFLATED)

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun liveStatus() = statusChannel.asFlow()

    @ExperimentalCoroutinesApi
    fun liveComments(storyId: String): Flow<Triple<FrontPageStory, List<FrontPageTag>, List<CommentView>>> {
        val story = lobstersQueries.getFrontPageStory(storyId).asFlow().mapToOne()
        val tags = lobstersQueries.getFrontPageTags().asFlow().mapToList()
        val comments = lobstersQueries.getComments(storyId).asFlow().mapToList()

        return story.combine(tags) { s, ts ->
            s to s.tags.mapNotNull { tag -> ts.find { it.tag == tag } }
        }.combine(comments) { (s, t), c ->
            Triple(s, t, c)
        }
    }

    @ExperimentalCoroutinesApi
    fun refresh(storyId: String, force: Boolean = false) {
        background.execute {
            val storyDb = lobstersQueries.getStory(storyId).executeAsOne()

            val shouldRefresh = if (force) true else {
                val comments = lobstersQueries.getComments(storyId).executeAsList()
                storyDb.insertedAt.isOld() || (comments.minBy {
                    it.insertedAt.time
                }?.insertedAt?.isOld() ?: true)
            }

            if (!shouldRefresh) {
                statusChannel.offer(LoadingStatus.DONE)
                return@execute
            }

            statusChannel.offer(LoadingStatus.LOADING)
            val newStory = lobstersService.getStorySync(storyId).executeOrNull()?.body()

            if (newStory == null) {
                statusChannel.offer(LoadingStatus.ERROR)
                return@execute
            }

            val now = Date()
            lobstersQueries.insertStory(newStory.toDB(storyDb.pageIndex, storyDb.pageSubIndex, now))
            newStory.comments?.forEachIndexed { i, c ->
                lobstersQueries.insertUser(c.commentingUser.toDB(now))
                lobstersQueries.insertComment(c.toDB(storyId, i, now))
            }

            statusChannel.offer(LoadingStatus.DONE)
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