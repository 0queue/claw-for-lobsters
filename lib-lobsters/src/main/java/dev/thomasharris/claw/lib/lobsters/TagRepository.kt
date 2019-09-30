package dev.thomasharris.claw.lib.lobsters

import javax.inject.Inject

class TagRepository @Inject constructor(
    private val lobstersService: LobstersService,
    private val lobstersQueries: LobstersQueries
) {

    private var tagCache: Map<String, FrontPageTag>? = null

    /**
     * API NOTE: some tags are not returned from tags.json
     *   but are colored special, namely #announce
     *
     * they are currently not handled
     */
    fun getFrontPageTagsSync(): Map<String, FrontPageTag> {

        tagCache?.let {
            return it
        }

        var tags = lobstersQueries.getFrontPageTags().executeAsList()

        // not the most efficient but there aren't a ton of tags out there
        if (tags.isEmpty() /* TODO or is old?*/) {
            lobstersService.getTagsSync().executeOrNull()?.body()?.forEach {
                lobstersQueries.insertTag(
                    TagDatabaseEntity.Impl(
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

            tags = lobstersQueries.getFrontPageTags().executeAsList()
        }

        tagCache = tags.map { it.tag to it }.toMap()

        return tagCache!!
    }

    fun invalidate() {
        tagCache = null
    }
}