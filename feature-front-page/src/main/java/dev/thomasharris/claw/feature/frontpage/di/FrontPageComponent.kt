package dev.thomasharris.claw.feature.frontpage.di

import dagger.Component
import dagger.Module
import dagger.Provides
import dev.thomasharris.claw.core.di.FeatureScope
import dev.thomasharris.claw.core.di.SingletonComponent
import dev.thomasharris.claw.feature.frontpage.StoryDataSourceFactory
import dev.thomasharris.claw.lib.lobsters.Event
import dev.thomasharris.claw.lib.lobsters.LoadingStatus
import dev.thomasharris.claw.lib.lobsters.StoryRepository
import dev.thomasharris.claw.lib.lobsters.TagRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

@Component(dependencies = [SingletonComponent::class], modules = [FrontPageModule::class])
@FeatureScope
interface FrontPageComponent {
    fun storyDataSourceFactory(): StoryDataSourceFactory

    fun storyRepositoryStatus(): Flow<Event<LoadingStatus>>
}

@Module
class FrontPageModule(private val scope: CoroutineScope) {
    @Provides
    @FeatureScope
    fun storyDataSourceFactory(
        storyRepository: StoryRepository,
        tagRepository: TagRepository
    ) = StoryDataSourceFactory(
        storyRepository,
        tagRepository,
        scope
    )

    @FlowPreview
    @ExperimentalCoroutinesApi
    @Provides
    fun provideStoryRepositoryStatus(storyRepository: StoryRepository) =
        storyRepository.liveStatus
}