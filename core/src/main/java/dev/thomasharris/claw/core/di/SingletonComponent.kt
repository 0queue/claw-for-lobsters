package dev.thomasharris.claw.core.di

import android.content.Context
import android.content.SharedPreferences
import dagger.Component
import dagger.Module
import dagger.Provides
import dev.thomasharris.claw.core.PreferencesRepository
import dev.thomasharris.claw.lib.lobsters.AsyncCommentsRepository
import dev.thomasharris.claw.lib.lobsters.AsyncStoryRepository
import dev.thomasharris.claw.lib.lobsters.AsyncUserRepository
import dev.thomasharris.claw.lib.lobsters.Database
import dev.thomasharris.claw.lib.lobsters.di.LobstersModule
import javax.inject.Singleton

@Component(modules = [LobstersModule::class, PrefsModule::class])
@Singleton
interface SingletonComponent {
    fun database(): Database

    val asyncStoryRepository: AsyncStoryRepository

    val asyncCommentsRepository: AsyncCommentsRepository

    val asyncUserRepository: AsyncUserRepository

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
