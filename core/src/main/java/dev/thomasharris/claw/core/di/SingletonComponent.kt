package dev.thomasharris.claw.core.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Component
import dagger.Module
import dagger.Provides
import dev.thomasharris.claw.core.PreferencesRepository
import dev.thomasharris.claw.lib.lobsters.AsyncStoryRepository
import dev.thomasharris.claw.lib.lobsters.AsyncTagRepository
import dev.thomasharris.claw.lib.lobsters.CommentRepository
import dev.thomasharris.claw.lib.lobsters.Database
import dev.thomasharris.claw.lib.lobsters.StoryRepository
import dev.thomasharris.claw.lib.lobsters.TagRepository
import dev.thomasharris.claw.lib.lobsters.di.LobstersModule
import javax.inject.Singleton

@Component(modules = [LobstersModule::class, PrefsModule::class])
@Singleton
interface SingletonComponent {
    fun database(): Database

    fun storyRepository(): StoryRepository

    val asyncStoryRepository: AsyncStoryRepository

    fun tagRepository(): TagRepository

    val asyncTagRepository: AsyncTagRepository

    fun commentRepository(): CommentRepository

    fun preferencesRepository(): PreferencesRepository
}

@Module
class PrefsModule(private var context: Context) {
    @Provides
    @Singleton
    fun providePreferences(): SharedPreferences {
        return context.getSharedPreferences(
            "dev.thomasharris.claw.GLOBAL_PREFS",
            Context.MODE_PRIVATE
        )
    }
}