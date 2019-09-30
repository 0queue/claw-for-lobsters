package dev.thomasharris.claw.feature.frontpage

import dev.thomasharris.claw.lib.lobsters.FrontPageStory
import dev.thomasharris.claw.lib.lobsters.FrontPageTag

sealed class FrontPageItem {
    data class Story(
        val frontPageStory: FrontPageStory,
        val tags: List<FrontPageTag>
    ) : FrontPageItem()

    data class Divider(val n: Int) : FrontPageItem()
}

fun FrontPageStory.toItem(tags: List<FrontPageTag>) = FrontPageItem.Story(this, tags)
