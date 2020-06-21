package dev.thomasharris.claw.lib.lobsters

import com.github.michaelbull.result.onSuccess
import com.github.michaelbull.result.runCatching
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AsyncTagRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val lobstersQueries: LobstersQueries
) {
    private var tagCache: Map<String, TagModel>? = null

    /**
     * API NOTE: some tags are not returned from tags.json
     *   but are colored special, namely #announce
     *
     * they are currently not handled
     */
    suspend fun getFrontPageTags(): Map<String, TagModel> = withContext(Dispatchers.IO) {

        tagCache?.let {
            return@withContext it
        }

        var tags = lobstersQueries.getTagModels().executeAsList()

        // not the most efficient but there aren't a ton of tags out there
        if (tags.isEmpty() /* TODO or is old?*/) {
            lobstersService.runCatching { getTags() }.onSuccess { newTags ->
                newTags.forEach {
                    lobstersQueries.insertTag(
                        Tag.Impl(
                            it.tag,
                            it.id,
                            it.description,
                            it.privileged,
                            it.isMedia,
                            it.inactive,
                            it.hotnessMod
                        )
                    )
                }
            }

            tags = lobstersQueries.getTagModels().list()
        }

        tagCache = tags.map { it.tag to it }.toMap()

        tagCache!!
    }

    fun invalidate() {
        tagCache = null
    }
}