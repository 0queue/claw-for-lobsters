package dev.thomasharris.lib.lobsters

import java.util.*
import javax.inject.Inject


class LobstersRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val lobstersDatabase: Database
) {
}

data class Story(
    val shortId: String,
    val createdAt: Date,
    val title: String,
    val url: String,
    val score: Int,
    val upvotes: Int,
    val downvotes: Int,
    val commentCount: Int,
    val description: String,
    val submitter: UserNetworkEntity,
    val tags: List<String>
)