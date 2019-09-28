package dev.thomasharris.claw.core.di

import dagger.Component
import dev.thomasharris.claw.lib.lobsters.CommentRepository
import dev.thomasharris.claw.lib.lobsters.Database
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

    fun commentRepository(): CommentRepository
}