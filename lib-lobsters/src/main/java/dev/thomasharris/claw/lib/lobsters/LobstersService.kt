package dev.thomasharris.claw.lib.lobsters

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.Date

interface LobstersService {

    /**
     * Hey this starts at 1, don't forget it
     */
    @GET("page/{index}.json")
    suspend fun getPage(@Path("index") index: Int): List<StoryNetworkEntity>

    @GET("s/{short_id}.json")
    suspend fun getStory(@Path("short_id") shortId: String): StoryNetworkEntity

    @GET("u/{username}.json")
    suspend fun getUser(@Path("username") username: String): UserNetworkEntity
}

typealias ShortId = String

@JsonClass(generateAdapter = true)
data class StoryNetworkEntity(
    @field:Json(name = "short_id") val shortId: ShortId,
    @field:Json(name = "short_id_url") val shortIdUrl: String,
    @field:Json(name = "created_at") val createdAt: Date,
    val title: String,
    val url: String,
    val score: Int,
    @field:Json(name = "comment_count") val commentCount: Int,
    val description: String,
    @field:Json(name = "submitter_user") val submitter: UserNetworkEntity,
    val tags: List<String>,
    val comments: List<CommentNetworkEntity>? = null,
)

@JsonClass(generateAdapter = true)
data class CommentNetworkEntity(
    @field:Json(name = "short_id") val shortId: ShortId,
    @field:Json(name = "short_id_url") val shortIdUrl: String,
    @field:Json(name = "created_at") val createdAt: Date,
    @field:Json(name = "updated_at") val updatedAt: Date,
    @field:Json(name = "is_deleted") val isDeleted: Boolean,
    @field:Json(name = "is_moderated") val isModerated: Boolean,
    val score: Int,
    val comment: String,
    val url: String,
    @field:Json(name = "indent_level") val indentLevel: Int, // starts at 1
    @field:Json(name = "commenting_user") val commentingUser: UserNetworkEntity,
)

@JsonClass(generateAdapter = true)
data class UserNetworkEntity(
    val username: String,
    @field:Json(name = "created_at") val createdAt: Date,
    @field:Json(name = "is_admin") val isAdmin: Boolean,
    val about: String,
    @field:Json(name = "is_moderator") val isModerator: Boolean,
    val karma: Int = 0,
    @field:Json(name = "avatar_url") val avatarUrl: String,
    @field:Json(name = "invited_by_user") val invitedByUser: String?,
    @field:Json(name = "github_username") val githubUsername: String?,
    @field:Json(name = "twitter_username") val twitterUsername: String?,
)

@JsonClass(generateAdapter = true)
data class TagNetworkEntity(
    val id: Int,
    val tag: String,
    val description: String,
    val privileged: Boolean,
    @field:Json(name = "is_media") val isMedia: Boolean,
    val inactive: Boolean,
    @field:Json(name = "hotness_mod") val hotnessMod: Float,
)
