package dev.thomasharris.claw.lib.lobsters

import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class StoryRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val lobstersQueries: LobstersQueries
) {

    fun getFrontPageSync(index: Int): List<StoryModel>? {
        // check db
        val dbPage = lobstersQueries.getPage(index).executeAsList()

        // should refresh?
        val isOld: Boolean? = dbPage.minBy {
            it.insertedAt.time
        }?.insertedAt?.let {
            TimeUnit.MILLISECONDS.toMinutes(Date().time - it.time) >= 60
        }

        if (dbPage.isNotEmpty() && isOld == false)
            return dbPage

        // fetch new page
        val newPage = lobstersService.getPageSync(index + 1).executeOrNull()?.body()
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

        // re fetch and return
        return lobstersQueries.getPage(index).executeAsList()
    }

    fun invalidate() {
        lobstersQueries.clear()
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