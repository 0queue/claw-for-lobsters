package dev.thomasharris.lib.lobsters

import com.squareup.moshi.Json
import retrofit2.http.GET
import retrofit2.http.Path
import java.util.*

interface LobstersService {
    @GET("page/{index}.json")
    suspend fun getPage(@Path("index") index: Int): List<StoryNetworkEntity>

    @GET("s/{short_id}.json")
    suspend fun getStory(@Path("short_id") shortId: ShortId): StoryNetworkEntity

    @GET("tags.json")
    suspend fun getTags(): List<TagNetworkEntity>
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
    val tags: List<String>
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
