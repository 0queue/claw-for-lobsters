package dev.thomasharris.claw.feature.comments

import dev.thomasharris.claw.core.PreferencesRepository
import dev.thomasharris.claw.core.di.FeatureScope
import dev.thomasharris.claw.lib.lobsters.CommentRepository
import javax.inject.Inject

@FeatureScope
class CollapseCommentUseCase @Inject constructor(
    private val commentRepository: CommentRepository,
    private val preferencesRepository: PreferencesRepository
) {
    fun collapse(shortId: String) = when (preferencesRepository.commentCollapseMode) {
        PreferencesRepository.CommentCollapseMode.MOBILE ->
            commentRepository.collapsePredecessors(shortId)
        PreferencesRepository.CommentCollapseMode.DESKTOP ->
            commentRepository.toggleCollapseComment(shortId)
    }
}