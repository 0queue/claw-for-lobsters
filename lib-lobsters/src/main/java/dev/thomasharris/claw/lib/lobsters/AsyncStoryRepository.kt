package dev.thomasharris.claw.lib.lobsters

import android.util.Log
import com.github.michaelbull.result.Ok
import com.github.michaelbull.result.Result
import com.github.michaelbull.result.map
import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AsyncStoryRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val lobstersQueries: LobstersQueries,
    private val tagRepository: AsyncTagRepository
) {

    /**
     * @param index 0 based index of page to fetch
     */
    suspend fun getFrontPage(index: Int, isRefresh: Boolean): Result<List<StoryWithTagsModel>, Throwable> =
        withContext(Dispatchers.IO) {

            val shouldRefresh = isRefresh && index == 0

            Log.i("AsyncStoryRepository", "Loading page $index")

            val oldestDate = lobstersQueries.getOldestStory(index).oneOrNull()

            val isOld: Boolean? = oldestDate?.min?.let {
                Date(it).isOld()
            } ?: true

            if (isOld == false && !shouldRefresh) {
                return@withContext Ok(lobstersQueries.getPage(index).list().withTags())
            }

            lobstersService.runCatching { getPage(index + 1) }
                .onSuccess { newPage ->
                    val now = Date()
                    lobstersQueries.transaction {
                        newPage.forEachIndexed { i, np ->
                            lobstersQueries.insertStory(np.toDB(index, i, now))
                            lobstersQueries.insertUser(np.submitter.toDB(now))
                        }

                        // if a story dropped off this page, trim remaining stories
                        if (lobstersQueries.getPageSize(index).executeAsOne() > 25)
                            lobstersQueries.trimExcess(index, 25)
                    }

                }
                .map {
                    // refetch
                    lobstersQueries.getPage(index).list().withTags()
                }
        }

    suspend fun getStory(storyId: String): StoryWithTagsModel? = withContext(Dispatchers.IO) {
        lobstersQueries.getStoryModel(storyId).oneOrNull()?.x(tagRepository.getFrontPageTags())
    }

    private suspend fun List<StoryModel>.withTags() = map { it x tagRepository.getFrontPageTags() }
}


fun StoryNetworkEntity.toDB(pageIndex: Int, subIndex: Int, now: Date = Date()) =
    Story.Impl(
        shortId,
        title,
        createdAt,
        url,
        score,
        upvotes,
        downvotes,
        commentCount,
        description,
        submitter.username,
        tags,
        pageIndex,
        subIndex,
        now
    )

fun UserNetworkEntity.toDB(now: Date = Date()) =
    User.Impl(
        username,
        createdAt,
        isAdmin,
        about,
        isModerator,
        karma,
        avatarUrl,
        invitedByUser,
        now
    )