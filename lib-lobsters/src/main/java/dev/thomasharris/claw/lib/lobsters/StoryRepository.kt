package dev.thomasharris.claw.lib.lobsters

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.withContext
import java.io.IOException
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class StoryRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val lobstersQueries: LobstersQueries
) {

    @ExperimentalCoroutinesApi
    private val statusChannel = BroadcastChannel<Event<LoadingStatus>>(CONFLATED)

    @ExperimentalCoroutinesApi
    @FlowPreview
    fun liveStatus() = statusChannel.asFlow()

    @ExperimentalCoroutinesApi
    fun getFrontPageSync(index: Int): List<StoryModel>? {
        // check db
        val dbPage = lobstersQueries.getPage(index).executeAsList()

        val oldestDate = lobstersQueries.getOldestStory(index).executeAsOne()
        // should refresh?
        val isOld: Boolean? = oldestDate.min?.let {
            Date(it).isOld()
        }

        if (dbPage.isNotEmpty() && isOld == false)
            return dbPage

        // fetch new page
        if (index == 0) statusChannel.offer(Event(LoadingStatus.LOADING))

        val newPage = lobstersService.getPageSync(index + 1).executeOrNull()?.body()

        if (index == 0 && newPage == null) statusChannel.offer(Event(LoadingStatus.ERROR))

        if (newPage == null && dbPage.isNotEmpty())
            return dbPage
        else if (newPage == null)
            return null

        // store new page

        val now = Date()
        newPage.forEachIndexed { i, np ->
            lobstersQueries.insertStory(np.toDB(index, i, now))
            lobstersQueries.insertUser(np.submitter.toDB(now))
        }

        // cascade if too many stories on this page (meaning a story dropped to a lower page)
        if (lobstersQueries.getPageSize(index).executeAsOne() > 25)
            getFrontPageSync(index + 1)

        if (index == 0) statusChannel.offer(Event(LoadingStatus.DONE))

        // re fetch and return
        return lobstersQueries.getPage(index).executeAsList()
    }

    /**
     * @return True if successfully refreshed, false otherwise
     */
    @ExperimentalCoroutinesApi
    suspend fun refresh(): Boolean = withContext(Dispatchers.IO) {
        statusChannel.offer(Event(LoadingStatus.LOADING))
        val newPage = try {
            lobstersService.getPage(1)
        } catch (ex: IOException) {
            statusChannel.offer(Event(LoadingStatus.ERROR))
            return@withContext false
        }

        val now = Date()
        lobstersQueries.transaction {
            lobstersQueries.clear()
            newPage.forEachIndexed { i, s ->
                lobstersQueries.insertStory(s.toDB(0, i, now))
                lobstersQueries.insertUser(s.submitter.toDB(now))
            }
        }

        statusChannel.offer(Event(LoadingStatus.DONE))

        true
    }
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