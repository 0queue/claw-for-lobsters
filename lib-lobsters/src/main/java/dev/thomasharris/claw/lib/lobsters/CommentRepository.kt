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
import java.util.Date
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
    fun liveVisibleComments(storyId: String): Flow<Triple<StoryModel, List<TagModel>, List<CommentModel>>> {
        val story = lobstersQueries.getStoryModel(storyId).asFlow().mapToOne()
        val tags = lobstersQueries.getTagModels().asFlow().mapToList()
        val comments = lobstersQueries.getVisibleCommentModels(storyId).asFlow().mapToList()

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

                val oldestComment = lobstersQueries.getOldestComment(storyId).executeAsOne()

                storyDb.insertedAt.isOld() || oldestComment.min?.let {
                    Date(it).isOld()
                } ?: true
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

    fun toggleCollapseComment(commentId: String) = background.execute {
        lobstersQueries.transaction {
            val status = lobstersQueries.getCommentStatus(commentId).executeAsOne()
            val shouldBeVisible = (status != CommentStatus.VISIBLE)
            lobstersQueries.setStatus(
                status = if (shouldBeVisible) CommentStatus.VISIBLE else CommentStatus.COLLAPSED,
                shortId = commentId
            )
            lobstersQueries.setChildrenStatus(
                status = if (shouldBeVisible) CommentStatus.VISIBLE else CommentStatus.GONE,
                commentId = commentId
            )
        }
    }
}

fun CommentNetworkEntity.toDB(
    storyId: String,
    index: Int,
    insertedAt: Date
) = Comment.Impl(
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
    insertedAt,
    CommentStatus.VISIBLE
)

fun Date.isOld(): Boolean {
    return TimeUnit.MILLISECONDS.toMinutes(Date().time - time) >= 60
}