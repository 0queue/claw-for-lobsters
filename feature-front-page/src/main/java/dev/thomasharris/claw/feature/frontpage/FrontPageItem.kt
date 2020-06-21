package dev.thomasharris.claw.feature.frontpage

import dev.thomasharris.claw.lib.lobsters.StoryWithTagsModel

sealed class FrontPageItem {
    data class Story(
        val story: StoryWithTagsModel
    ) : FrontPageItem()

    data class Divider(val n: Int) : FrontPageItem()
}
