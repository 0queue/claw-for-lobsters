package dev.thomasharris.lib.lobsters

import javax.inject.Inject

class TagRepository @Inject constructor(
    private val lobstersService: LobstersService
) {
    private var tagMap: MutableMap<String, TagNetworkEntity> = hashMapOf()

    /**
     * TODO store offline
     */
    fun getTagsSync(): Map<String, TagNetworkEntity> {
        if (tagMap.isEmpty())
            tagMap = lobstersService.getTagsSync().execute().body()?.map {
                it.tag to it
            }?.toMap()?.toMutableMap() ?: hashMapOf()

        return tagMap
    }
}