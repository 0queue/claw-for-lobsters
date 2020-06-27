package dev.thomasharris.claw.lib.lobsters

import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import java.util.Date
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val lobstersQueries: LobstersQueries,
    private val storyRepository: AsyncStoryRepository,
    private val background: Executor
) {

    @ExperimentalCoroutinesApi
    private val statusChannel = BroadcastChannel<Event<LoadingStatus>>(CONFLATED)

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun liveStatus() = statusChannel.asFlow()

    @ExperimentalCoroutinesApi
    fun liveVisibleComments(storyId: String): Flow<Pair<StoryWithTagsModel?, List<CommentModel>>> {
        val story = flow {  emit(storyRepository.getStory(storyId)) }
        val comments = lobstersQueries.getVisibleCommentModels(storyId).asFlow().mapToList()

        return story.combine(comments) { s, c ->
            s to c
        }
    }

    @ExperimentalCoroutinesApi
    fun refresh(storyId: String, force: Boolean = false) {
        background.execute {
            val storyDb = lobstersQueries.getStory(storyId).executeAsOneOrNull()

            val shouldRefresh = if (force) true else {

                val oldestComment = lobstersQueries.getOldestComment(storyId).executeAsOne()

                storyDb?.insertedAt?.isOld() ?: true || oldestComment.min?.let {
                    Date(it).isOld()
                } ?: true
            }

            if (!shouldRefresh) {
                statusChannel.offer(Event(LoadingStatus.DONE))
                return@execute
            }

            statusChannel.offer(Event(LoadingStatus.LOADING))
            val newStory = lobstersService.getStorySync(storyId).executeOrNull()?.body()

            if (newStory == null) {
                statusChannel.offer(Event(LoadingStatus.ERROR))
                return@execute
            }

            val now = Date()
            // if story db is null here, we jumped here from an intent
            val pageIndex = storyDb?.pageIndex ?: -1
            val subIndex = storyDb?.pageSubIndex ?: -1
            lobstersQueries.transaction {
                lobstersQueries.insertStory(newStory.toDB(pageIndex, subIndex, now))
                newStory.comments?.forEachIndexed { i, c ->
                    lobstersQueries.insertUser(c.commentingUser.toDB(now))
                    lobstersQueries.insertComment(c.toDB(storyId, i, now))
                }
            }

            statusChannel.offer(Event(LoadingStatus.DONE))
        }
    }

    /**
     * Expand if collapsed, else collapse self
     */
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

    /**
     * Expand if collapsed, else collapse predecessors and predecessors
     * of parents
     */
    fun collapsePredecessors(commentId: String) = background.execute {
        lobstersQueries.transaction {
            val status = lobstersQueries.getCommentStatus(commentId).executeAsOne()
            if (status != CommentStatus.VISIBLE) {
                lobstersQueries.setStatus(status = CommentStatus.VISIBLE, shortId = commentId)
                lobstersQueries.setChildrenStatus(
                    status = CommentStatus.VISIBLE,
                    commentId = commentId
                )
            } else ancestors(commentId).map {
                lobstersQueries.getPredecessors(it).executeAsList()
            }.flatten().forEach {
                lobstersQueries.setStatus(status = CommentStatus.COLLAPSED, shortId = it)
                lobstersQueries.setChildrenStatus(status = CommentStatus.GONE, commentId = it)
            }
        }
    }

    // wow I was wondering when tailrec would be useful
    private tailrec fun ancestors(
        shortId: String,
        progress: List<String> = listOf(shortId)
    ): List<String> {
        val next = lobstersQueries.getParent(shortId).executeAsOneOrNull() ?: return progress
        return ancestors(next, progress + next)
    }
}

fun CommentNetworkEntity.toDB(
    storyId: String,
    index: Int,
    insertedAt: Date
) = Comment(
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