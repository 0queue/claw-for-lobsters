package dev.thomasharris.claw.feature.frontpage

import dev.thomasharris.claw.lib.lobsters.StoryModel
import dev.thomasharris.claw.lib.lobsters.TagModel

sealed class FrontPageItem {
    data class Story(
        val frontPageStory: StoryModel,
        val tags: List<TagModel>
    ) : FrontPageItem()

    data class Divider(val n: Int) : FrontPageItem()
}

fun StoryModel.toItem(tags: List<TagModel>) = FrontPageItem.Story(this, tags)
