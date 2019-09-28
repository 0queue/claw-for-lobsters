package dev.thomasharris.claw.lib.lobsters

import com.squareup.moshi.Json
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import java.io.IOException
import java.util.*

interface LobstersService {

    /**
     * Hey this starts at 1, don't forget it
     */
    @GET("page/{index}.json")
    suspend fun getPage(@Path("index") index: Int): List<StoryNetworkEntity>

    @GET("page/{index}.json")
    fun getPageSync(@Path("index") index: Int): Call<List<StoryNetworkEntity>>

    @GET("s/{short_id}.json")
    suspend fun getStory(@Path("short_id") shortId: ShortId): StoryNetworkEntity

    @GET("s/{short_id}.json")
    fun getStorySync(@Path("short_id") shortId: String): Call<StoryNetworkEntity>

    @GET("tags.json")
    suspend fun getTags(): List<TagNetworkEntity>

    @GET("tags.json")
    fun getTagsSync(): Call<List<TagNetworkEntity>>
}

typealias ShortId = String

data class StoryNetworkEntity(
    @field:Json(name = "short_id") val shortId: ShortId,
    @field:Json(name = "short_id_url") val shortIdUrl: String,
    @field:Json(name = "created_at") val createdAt: Date,
    val title: String,
    val url: String,
    val score: Int,
    val upvotes: Int,
    val downvotes: Int,
    @field:Json(name = "comment_count") val commentCount: Int,
    val description: String,
    @field:Json(name = "submitter_user") val submitter: UserNetworkEntity,
    val tags: List<String>,
    val comments: List<CommentNetworkEntity>? = null
)

data class CommentNetworkEntity(
    @field:Json(name = "short_id") val shortId: ShortId,
    @field:Json(name = "short_id_url") val shortIdUrl: String,
    @field:Json(name = "created_at") val createdAt: Date,
    @field:Json(name = "updated_at") val updatedAt: Date,
    @field:Json(name = "is_deleted") val isDeleted: Boolean,
    @field:Json(name = "is_moderated") val isModerated: Boolean,
    val score: Int,
    val upvotes: Int,
    val downvotes: Int,
    val comment: String,
    val url: String,
    @field:Json(name = "indent_level") val indentLevel: Int, // starts at 1
    @field:Json(name = "commenting_user") val commentingUser: UserNetworkEntity
)

data class UserNetworkEntity(
    val username: String,
    @field:Json(name = "created_at") val createdAt: Date,
    @field:Json(name = "is_admin") val isAdmin: Boolean,
    val about: String,
    @field:Json(name = "is_moderator") val isModerator: Boolean,
    val karma: Int,
    @field:Json(name = "avatar_url") val avatarUrl: String,
    @field:Json(name = "invited_by_user") val invitedByUser: String
)

data class TagNetworkEntity(
    val id: Int,
    val tag: String,
    val description: String,
    val privileged: Boolean,
    @field:Json(name = "is_media") val isMedia: Boolean,
    val inactive: Boolean,
    @field:Json(name = "hotness_mod") val hotnessMod: Float
)

fun <T> Call<T>.executeOrNull(): Response<T>? = try {
    execute()
} catch (e: IOException) {
    null
}