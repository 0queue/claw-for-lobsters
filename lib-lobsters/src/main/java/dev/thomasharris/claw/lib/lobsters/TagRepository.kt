package dev.thomasharris.claw.lib.lobsters

import javax.inject.Inject

class TagRepository @Inject constructor(
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
    fun getFrontPageTagsSync(): Map<String, TagModel> {

        tagCache?.let {
            return it
        }

        var tags = lobstersQueries.getTagModels().executeAsList()

        // not the most efficient but there aren't a ton of tags out there
        if (tags.isEmpty() /* TODO or is old?*/) {
            lobstersService.getTagsSync().executeOrNull()?.body()?.forEach {
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

            tags = lobstersQueries.getTagModels().executeAsList()
        }

        tagCache = tags.map { it.tag to it }.toMap()

        return tagCache!!
    }

    fun invalidate() {
        tagCache = null
    }
}