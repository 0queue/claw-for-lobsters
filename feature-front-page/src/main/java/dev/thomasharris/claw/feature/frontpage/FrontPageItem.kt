package dev.thomasharris.claw.feature.frontpage

import dev.thomasharris.claw.lib.lobsters.StoryModel

sealed class FrontPageItem {
    data class Story(
        val story: StoryModel
    ) : FrontPageItem()

    data class Divider(val n: Int) : FrontPageItem()
}
