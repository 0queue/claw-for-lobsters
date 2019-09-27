package dev.thomasharris.feature.frontpage.di

import dagger.Component
import dagger.Module
import dagger.Provides
import dev.thomasharris.claw.core.di.FeatureScope
import dev.thomasharris.claw.core.di.SingletonComponent
import dev.thomasharris.feature.frontpage.StoryDataSourceFactory
import dev.thomasharris.lib.lobsters.StoryRepository
import dev.thomasharris.lib.lobsters.TagRepository

@Component(dependencies = [SingletonComponent::class], modules = [FrontPageModule::class])
@FeatureScope
interface FrontPageComponent {
    fun storyDataSourceFactory(): StoryDataSourceFactory
}

@Module
class FrontPageModule {
    @Provides
    @FeatureScope
    fun storyDataSourceFactory(
        storyRepository: StoryRepository,
        tagRepository: TagRepository
    ) = StoryDataSourceFactory(storyRepository, tagRepository)
}