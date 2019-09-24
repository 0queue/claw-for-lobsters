package dev.thomasharris.feature.frontpage.di

import com.squareup.sqldelight.android.paging.QueryDataSourceFactory
import dagger.Component
import dagger.Module
import dagger.Provides
import dev.thomasharris.claw.core.di.FeatureScope
import dev.thomasharris.claw.core.di.SingletonComponent
import dev.thomasharris.lib.lobsters.Database
import dev.thomasharris.lib.lobsters.StoryDatabaseEntity

@Component(dependencies = [SingletonComponent::class], modules = [FrontPageModule::class])
@FeatureScope
interface FrontPageComponent {
    fun queryDataSourceFactory(): QueryDataSourceFactory<StoryDatabaseEntity>
}

@Module
class FrontPageModule {

    @Provides
    @FeatureScope
    fun queryDataSourceFactory(database: Database): QueryDataSourceFactory<StoryDatabaseEntity> {
        return QueryDataSourceFactory(
            queryProvider = database.storyDatabaseEntityQueries::storiesPaged,
            countQuery = database.storyDatabaseEntityQueries.countStories()
        )
    }
}