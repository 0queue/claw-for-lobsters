package dev.thomasharris.claw.feature.comments.di

import dagger.Component
import dagger.Module
import dev.thomasharris.claw.core.di.FeatureScope
import dev.thomasharris.claw.core.di.SingletonComponent
import dev.thomasharris.claw.lib.lobsters.LobstersService

@Component(dependencies = [SingletonComponent::class], modules = [CommentsModule::class])
@FeatureScope
interface CommentsComponent {
    fun lobstersService(): LobstersService
}

@Module
class CommentsModule {

}