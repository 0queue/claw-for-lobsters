package dev.thomasharris.claw.feature.userprofile.di

import dagger.Component
import dev.thomasharris.claw.core.di.FeatureScope
import dev.thomasharris.claw.core.di.SingletonComponent
import dev.thomasharris.claw.lib.lobsters.AsyncUserRepository

@Component(dependencies = [SingletonComponent::class])
@FeatureScope
interface UserProfileComponent {
    val userRepository: AsyncUserRepository
}
