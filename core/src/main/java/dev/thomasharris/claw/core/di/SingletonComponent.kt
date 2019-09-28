package dev.thomasharris.claw.core.di

import dagger.Component
import dev.thomasharris.claw.lib.lobsters.Database
import dev.thomasharris.claw.lib.lobsters.LobstersService
import dev.thomasharris.claw.lib.lobsters.StoryRepository
import dev.thomasharris.claw.lib.lobsters.TagRepository
import dev.thomasharris.claw.lib.lobsters.di.LobstersModule
import javax.inject.Singleton

@Component(modules = [LobstersModule::class])
@Singleton
interface SingletonComponent {
    fun database(): Database

    fun storyRepository(): StoryRepository

    fun tagRepository(): TagRepository

    // TODO for getting off the ground ONLY
    fun lobstersService(): LobstersService
}