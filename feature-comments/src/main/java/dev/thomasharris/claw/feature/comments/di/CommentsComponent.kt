package dev.thomasharris.claw.feature.comments.di

import dagger.Component
import dev.thomasharris.claw.core.di.FeatureScope
import dev.thomasharris.claw.core.di.SingletonComponent
import dev.thomasharris.claw.lib.lobsters.CommentRepository

@Component(dependencies = [SingletonComponent::class])
@FeatureScope
interface CommentsComponent {
    fun commentRepository(): CommentRepository
}