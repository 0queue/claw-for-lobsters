package dev.thomasharris.claw.feature.frontpage.di

import dagger.Component
import dagger.Module
import dagger.Provides
import dev.thomasharris.claw.core.di.FeatureScope
import dev.thomasharris.claw.core.di.SingletonComponent
import dev.thomasharris.claw.feature.frontpage.paging3.FrontPagePagingSource
import dev.thomasharris.claw.lib.lobsters.AsyncTagRepository
import dev.thomasharris.claw.lib.lobsters.StoryRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

@Component(dependencies = [SingletonComponent::class])
@FeatureScope
interface FrontPageComponent {
//    fun storyDataSourceFactoryFactory(): StoryDataSourceFactoryFactory

//    fun storyRepositoryStatus(): Flow<Event<LoadingStatus>>

    val frontPagePagingSource: FrontPagePagingSource

    val tagRepository: AsyncTagRepository
}

@Module
class FrontPageModule {
    @FlowPreview
    @ExperimentalCoroutinesApi
    @Provides
    fun provideStoryRepositoryStatus(storyRepository: StoryRepository) =
        storyRepository.liveStatus()
}