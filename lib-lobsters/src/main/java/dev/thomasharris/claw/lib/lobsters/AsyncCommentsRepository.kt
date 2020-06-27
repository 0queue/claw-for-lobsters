package dev.thomasharris.claw.lib.lobsters

import com.github.michaelbull.result.getOr
import com.github.michaelbull.result.runCatching
import com.squareup.sqldelight.runtime.coroutines.asFlow
import com.squareup.sqldelight.runtime.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import java.util.Date
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Suppress("EXPERIMENTAL_API_USAGE")
@Singleton
class AsyncCommentsRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val lobstersQueries: LobstersQueries,
    private val storyRepository: AsyncStoryRepository,
    private val background: Executor
) {

    private val _status = MutableStateFlow(LoadingStatus.NOT_LOADING.event())
    val status: StateFlow<Event<LoadingStatus>>
        get() = _status

    fun visibleComments(storyId: String): Flow<Pair<StoryModel?, List<CommentModel>>> {
        val story = flow { emit(storyRepository.getStory(storyId)) }
        val comments = lobstersQueries.getVisibleCommentModels(storyId).asFlow().mapToList()

        return story.combine(comments) { s, c -> s to c }
    }

    suspend fun refresh(storyId: String, force: Boolean = false) = withContext(Dispatchers.IO) {
        val story = lobstersQueries.getStory(storyId).oneOrNull()

        val shouldRefresh = if (force) true else {
            val oldestComment = lobstersQueries.getOldestComment(storyId).one()
            story?.insertedAt.isOld() || oldestComment.min?.let(::Date).isOld()
        }

        if (!shouldRefresh) {
            _status.value = LoadingStatus.NOT_LOADING.event()
            return@withContext
        }

        _status.value = LoadingStatus.LOADING.event()

        val newStory = lobstersService.runCatching {
            getStory(storyId)
        }.getOr {
            _status.value = LoadingStatus.ERROR.event()
            return@withContext
        }

        val now = Date()

        // if story db is null, we jumped from an intent
        val pageIndex = story?.pageIndex ?: -1
        val subIndex = story?.pageSubIndex ?: -1

        lobstersQueries.transaction {
            lobstersQueries.insertStory(newStory.toDB(pageIndex, subIndex, now))
            newStory.comments?.forEachIndexed { i, c ->
                lobstersQueries.insertUser(c.commentingUser.toDB(now))
                lobstersQueries.insertComment(c.toDB(storyId, i, now))
            }
        }

        _status.value = LoadingStatus.NOT_LOADING.event()
    }

    // not async because a. it's fire and forget and b. it's all a transaction,
    // which aren't very cooperative with suspending and such
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

fun Date?.isOld(): Boolean {
    if (this == null)
        return true

    return TimeUnit.MILLISECONDS.toMinutes(Date().time - time) >= 60
}