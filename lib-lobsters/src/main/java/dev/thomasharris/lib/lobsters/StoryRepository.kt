package dev.thomasharris.lib.lobsters

import java.util.*
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class StoryRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val lobstersQueries: LobstersQueries
) {

    // use a callback instead of return value so that when
    // a new page is fetched the stories can be inserted
    // after sending them back to the paging library
//    fun getPageSync(index: Int, callback: (List<Story>?) -> Unit) {
//
//        // get offline page
//        val dbPage = lobstersQueries.getPage(index.toLong()).executeAsList()
//
//        // check age
//        val isOld: Boolean? = dbPage.minBy {
//            it.insertedAt.time
//        }?.insertedAt?.let {
//            TimeUnit.MILLISECONDS.toMinutes(Date().time - it.time) >= 60
//        }
//
//        val tagMap = tagRepository.getTagsSync()
//
//        // if page not downloaded, or is old...
//        if (dbPage.isEmpty() || isOld == true) {
//            // fetch new page
//            val newPage = lobstersService.getPageSync(index + 1).executeOrNull()?.body()?.map {
//                Story.from(it, tagMap)
//            }
//
//            // return new page
//            callback(newPage)
//
//            // store new page
//            val now = Date()
//            newPage?.forEach {
//                lobstersQueries.insertStory(it.toDatabaseEntity(index, now))
//            }
//        } else
//            callback(dbPage.map {
//                Story.from(it, tagMap)
//            })
//    }

    fun getFrontPageSync(index: Int): List<FrontPageStory>? {
        // check db
        val dbPage = lobstersQueries.getFrontPage(index).executeAsList()

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
        newPage.forEach {
            lobstersQueries.insertStory(it.toDB(index, now))
            lobstersQueries.insertUser(it.submitter.toDB(now))
        }

        // re fetch and return
        return lobstersQueries.getFrontPage(index).executeAsList()
    }

    fun invalidate() {
        lobstersQueries.clear()
    }
}

fun StoryNetworkEntity.toDB(index: Int, now: Date = Date()) = StoryDatabaseEntity.Impl(
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
    index,
    now
)

fun UserNetworkEntity.toDB(now: Date = Date()) = UserDatabaseEntity.Impl(
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

//data class Story(
//    val shortId: String,
//    val createdAt: Date,
//    val title: String,
//    val url: String,
//    val score: Int,
//    val upvotes: Int,
//    val downvotes: Int,
//    val commentCount: Int,
//    val description: String,
//    val submitterUsername: String,
//    val submitterAvatarURL: String,
//    val tags: List<TagNetworkEntity>
//) {
//
//    fun toDatabaseEntity(
//        index: Int,
//        insertedAt: Date = Date()
//    ): StoryDatabaseEntity {
//        return StoryDatabaseEntity.Impl(
//            shortId,
//            title,
//            createdAt,
//            url,
//            score.toLong(),
//            upvotes.toLong(),
//            downvotes.toLong(),
//            commentCount.toLong(),
//            description,
//            submitterUsername,
//            submitterAvatarURL,
//            tags.map(TagNetworkEntity::tag),
//            index.toLong(),
//            insertedAt
//        )
//    }
//
//    companion object {
//        fun from(
//            storyNetworkEntity: StoryNetworkEntity,
//            tagMap: Map<String, TagNetworkEntity>
//        ): Story {
//            with(storyNetworkEntity) {
//                return Story(
//                    shortId,
//                    createdAt,
//                    title,
//                    url,
//                    score,
//                    upvotes,
//                    downvotes,
//                    commentCount,
//                    description,
//                    submitter.username,
//                    submitter.avatarUrl,
//                    tags.mapNotNull { tagMap[it] }
//                )
//            }
//        }
//
//        fun from(
//            storyDatabaseEntity: StoryDatabaseEntity,
//            tagMap: Map<String, TagNetworkEntity>
//        ): Story {
//            with(storyDatabaseEntity) {
//                return Story(
//                    shortId,
//                    createdAt,
//                    title,
//                    url,
//                    score.toInt(),
//                    upvotes.toInt(),
//                    downvotes.toInt(),
//                    commentCount.toInt(),
//                    description,
//                    submitterUsername,
//                    submitterAvatarShortURL,
//                    tags.mapNotNull { tagMap[it] }
//                )
//            }
//        }
//    }
//}