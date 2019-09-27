package dev.thomasharris.claw.core.di

import dagger.Component
import dev.thomasharris.lib.lobsters.Database
import dev.thomasharris.lib.lobsters.StoryRepository
import dev.thomasharris.lib.lobsters.TagRepository
import dev.thomasharris.lib.lobsters.di.LobstersModule
import javax.inject.Singleton

@Component(modules = [LobstersModule::class])
@Singleton
interface SingletonComponent {
    fun database(): Database

    fun storyRepository(): StoryRepository

    fun tagRepository(): TagRepository
}